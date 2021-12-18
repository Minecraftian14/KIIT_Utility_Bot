package in.mcxiv.disapp;

import com.mcxiv.logger.decorations.Decorations;
import com.mcxiv.logger.decorations.Format;
import com.mcxiv.logger.formatted.FLog;
import com.mcxiv.logger.formatted.fixed.FileLog;
import com.mcxiv.logger.ultimate.ULog;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.mail.Message;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.security.auth.login.LoginException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Format(":#aaf n:.::")
public class PrototypeActivity extends ListenerAdapter {

    private static final Pattern rx_KIITMailID = Pattern.compile("([\\d]{4})([\\d]{4})@kiit\\.ac\\.in");
    private static final FLog log;

    private static final int CORES;

    private static final String BOT_TOKEN;
    private static final String LINK;

    private static final String SMTP_MAIL_SENDER;
    private static final String SMTP_APPLICATION_PASS;
    private static final String MAIL_USER_NAME;
    private static final String SMTP_HOST_NAME = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";
    private static final Properties SMTP_ENVIRONMENT_PROPERTIES = new Properties();

    static {

        // https://github.com/DV8FromTheWorld/JDA/issues/1858#issuecomment-942066283
        CORES = Runtime.getRuntime().availableProcessors();
        System.out.printf("We are running on a total of %d cores!%n", CORES);
        if (CORES <= 1) {
            System.out.println("Welp... Setting up Parallelism Flag.");
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1");
        }

        FLog file_logger = FileLog.getNew("KIIT Bot Utility.log");
        FLog strm_logger = FLog.getNew();
        file_logger.setDecorationType(Decorations.TAG);
        strm_logger.setDecorationType(Decorations.CONSOLE);
        log = ULog.forNew().add(file_logger).add(strm_logger).create();
        log.prt(LocalDateTime.now());

        ResourceBundle bundle = ResourceBundle.getBundle("env");
        BOT_TOKEN = bundle.getString("bot_token");
        LINK = bundle.getString("invite_link");
        SMTP_MAIL_SENDER = bundle.getString("mail_from");
        SMTP_APPLICATION_PASS = bundle.getString("app_pass");
        MAIL_USER_NAME = bundle.getString("user_name");

        SMTP_ENVIRONMENT_PROPERTIES.put("mail.smtp.user", SMTP_MAIL_SENDER);
        SMTP_ENVIRONMENT_PROPERTIES.put("mail.smtp.host", SMTP_HOST_NAME);
        SMTP_ENVIRONMENT_PROPERTIES.put("mail.smtp.port", SMTP_PORT);
        SMTP_ENVIRONMENT_PROPERTIES.put("mail.smtp.starttls.enable", "true");
        SMTP_ENVIRONMENT_PROPERTIES.put("mail.smtp.debug", "true");
        SMTP_ENVIRONMENT_PROPERTIES.put("mail.smtp.auth", "true");
        SMTP_ENVIRONMENT_PROPERTIES.put("mail.smtp.socketFactory.port", SMTP_PORT);
        SMTP_ENVIRONMENT_PROPERTIES.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        SMTP_ENVIRONMENT_PROPERTIES.put("mail.smtp.socketFactory.fallback", "false");
    }

    private static final String fmt_REQUEST_MAIL_ID = """
            **WELCOME TO B20's PRIVATE SERVER**
            Sorry for the inconvenience but we need to make sure that only members of B20 batch can join this server!
            Therefore, I request you to give me your KIIT mail ID __on the server__ so that I can share you an OTP.
            """;

    private static final String fmt_MAIL_SENT_NOTICE = """
            Thanks a lot for sharing me your mail ID!
            I have sent you a mail with an OTP. Can you please sent me that OTP here?
            """;

    private static final String fmt_VERIFIED = """
            You have been successfully verified.
            Thanks a lot.
            """;

    private static final String fmt_OTP_MAIL = """
            WELCOME TO B20's PRIVATE SERVER
            Your OTP is %d. Please send this OTP to the verification bot named KIIT Utility Bot.
            Thanks a lot.
            """;

    private static final String fmt_ALREADY_VERIFIED = """
            Bro... I appreciate the enthusiasm...
            Just a note between among us.
            You're already verified xD
            """;

    @Format({":: :#0f0 <hh;mm;ss> %*20s b: ::", ":#afa n:.::"})
    public static void notice(String title, String msg) {
        log.prt(title, msg);
    }

    @Format({":: :#f00 <hh;mm;ss> %*20s b: ::", ":#faa n:.::"})
    public static void error(String title, String msg) {
        log.prt(title, msg);
    }

    public static void errorAndExit(String title, String msg) {
        error(title, msg);
        System.exit(-1);
    }

    public static void main(String[] args) throws LoginException, InterruptedException {

        notice("Main Method", "Application is being started");

        PrototypeActivity activity = new PrototypeActivity();

        JDABuilder builder = JDABuilder.createDefault(BOT_TOKEN);
        builder.setActivity(Activity.watching("the world!"));
        builder.addEventListeners(activity);
        log.prt("JDA builder built");

        Thread inputThread = new Thread(() -> {
            new Scanner(System.in).next();
            notice("App Closing", "The application is going to shut down.");
            System.exit(0);
        });
        inputThread.setPriority(Thread.MIN_PRIORITY);
        inputThread.start();

        JDA jda = builder.build();
        log.prt("JDA starting");
        jda.awaitReady();
    }

    private List<MemberRecord> listOfOTPsSent = new ArrayList<>();

    private Guild GUILD;
    private Role MEMBER_ROLE;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        List<Guild> guilds = event.getJDA().getGuilds();

        if (guilds.size() < 1)
            errorAndExit("No Guilds!", "Heya buddy. Please add the bot in at least one Guild!");

        GUILD = guilds.get(0);

        List<Role> roles = GUILD.getRolesByName("Member", false);

        if (roles.size() < 1)
            errorAndExit("No Roles", "Heya buddy. Please add at least one role named Member!");

        MEMBER_ROLE = roles.get(0);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.prtf(":$Rn:").consume("Exiting System (or probably already exited...)!");
            List<String> list = listOfOTPsSent.stream().map(MemberRecord::toString).toList();
            for (int i = 0; i < list.size(); i++)
                log.prtf(":%3s #00f: -> ::", ":#aaf n:").consume("" + i, list.get(i));
        }));

        notice("Bot Active", "It's initialised, cached and ready!");
    }

    private boolean isVerified(Member member) {
        return member.getRoles().contains(MEMBER_ROLE);
    }

    private boolean isBeingProcessed(User user) {
        return listOfOTPsSent.stream().anyMatch(memberRecord -> memberRecord.user.equals(user));
    }

    private MemberRecord getMR(User user) {
        return listOfOTPsSent
                .stream()
                .filter(memberRecord -> memberRecord.user.equals(user))
                .findFirst()
                .orElseThrow();
    }

    private Member getMember(MessageReceivedEvent event, User user) {
        Member member = event.getMember();
        if (member != null) return member;
        return member = GUILD.retrieveMember(user).complete();
    }

    private static void addRole(Guild guild, Member member, User user) {
        guild.addRoleToMember(member, guild.getRolesByName("Member", false).get(0)).queue();
        user.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(fmt_VERIFIED).queue());
        log.prt("Role given to %s".formatted(user.getAsTag()));
    }

    private void sendVerificationMail(String m_to, int otp) {

        Session session = Session.getInstance(SMTP_ENVIRONMENT_PROPERTIES, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_MAIL_SENDER, SMTP_APPLICATION_PASS);
            }
        });

        session.setDebug(false);

        MimeMessage msg = new MimeMessage(session);
        try {
            msg.setSubject("Verification Requested!");
            msg.setFrom(new InternetAddress(SMTP_MAIL_SENDER));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(m_to));
            msg.setText(fmt_OTP_MAIL.formatted(otp));

            Transport transport = session.getTransport("smtps");
            transport.connect(SMTP_HOST_NAME, Integer.parseInt(SMTP_PORT), MAIL_USER_NAME, SMTP_APPLICATION_PASS);
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void sendDM(User user, String message) {
        sendDM(user, privateChannel -> privateChannel.sendMessage(message).queue());
    }

    private void sendDM(User user, Consumer<PrivateChannel> channel) {
        user.openPrivateChannel().queue(channel);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {

        User author = event.getUser();
        Member member = event.getMember();

        if (author.isBot()) return;

        notice("New Member", "%s joined the server!".formatted(author.getName()));

        author.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage(fmt_REQUEST_MAIL_ID).queue();
            log.prt("Request DM sent");
        });
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        User author = event.getAuthor();
        Member member = getMember(event, author);
        String contentRaw = event.getMessage().getContentRaw().toLowerCase();

        if (author.isBot()) return;

        if (isBeingProcessed(author)) {
            MemberRecord memberRecord = getMR(author);
            if (contentRaw.contains(String.valueOf(memberRecord.otp))) {
                log.prt("OPT %d Verified".formatted(memberRecord.otp));
                memberRecord.addMemberRole();
                listOfOTPsSent.remove(memberRecord);
            }
            return;
        }

        if (member == null) return;

        if (!contentRaw.contains("verify")) return;
        if (isVerified(member)) {
            sendDM(author, fmt_ALREADY_VERIFIED);
            return;
        }

        Matcher matcher = rx_KIITMailID.matcher(contentRaw);
        if (!matcher.find()) return;

        short sessionale = Short.parseShort(matcher.group(1));
        short rollnumish = Short.parseShort(matcher.group(2));
        String mail_id = matcher.group();

        if (sessionale != 2105) return;
        if (rollnumish < 2041 || rollnumish > 2125) return;

        notice("Verification Req", "%1$s wants to get verified. %1$s's roll number is %2$d%3$d.".formatted(author.getAsTag(), sessionale, rollnumish));
        event.getMessage().addReaction("\uD83D\uDC4D").queue();

        author.openPrivateChannel().queue(privateChannel -> {
            /*int otp = (int) ((Math.random() * (999999 - 100000)) + 100000);
            other method*/
            Random random = new Random();
            int otp = random.ints(100000, 999999).findFirst().getAsInt();
            
            listOfOTPsSent.add(new MemberRecord(member, author, GUILD, otp));
            log.prt("Sending mail");
            sendVerificationMail(mail_id, otp);
            log.prt("Mail sent! Sending DM");
            privateChannel.sendMessage(fmt_MAIL_SENT_NOTICE).queue();
            log.prt("DM sent");
        });
    }

    record MemberRecord(Member member, User user, Guild guild, Integer otp) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (o instanceof MemberRecord that)
                return Objects.equals(member, that.member) || Objects.equals(user, that.user) || Objects.equals(otp, that.otp);
            if (o instanceof Member that) return Objects.equals(member, that);
            if (o instanceof User that) return Objects.equals(user, that);
            if (o instanceof Integer that) return Objects.equals(otp, that);
            return false;
        }

        public void addMemberRole() {
            addRole(guild, member, user);
        }
    }

}























