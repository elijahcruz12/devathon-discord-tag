package com.outdatedversion;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.SimpleLog;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

/**
 * OutdatedVersion
 * Oct/30/2016 (11:16 PM)
 */

public class DevathonTagApplier extends ListenerAdapter
{

    // Discord bot auth:
    // https://discordapp.com/api/oauth2/authorize?client_id=242502184195850241&scope=bot&permissions=268438528

    private static final char COMMAND_PREFIX = '!';
    private static final LocalDateTime STARTING_AT = LocalDateTime.of(2016, Month.NOVEMBER, 5, 12, 0);

    private final JDA jda;
    private final Checker checker;
    private final SimpleLog logger;

    /** the channel that we're processing requests from */
    private TextChannel channel;

    /** the Discord server we're operating from */
    private Guild guild;

    /** the {@link Role} that we apply to users */
    private Role contestantRole;

    /**
     * starts up our bot
     *
     * @param args the args from startup
     *             0 - the bot token
     *             1 - the ID of the guild
     *             2 - the name of the channel
     *                 used to apply ranks from
     *             3 - the name of the role we apply
     */
    public DevathonTagApplier(final String[] args) throws Exception
    {
        checker = new Checker();
        logger = setupLogger();

        jda = new JDABuilder()
                    .setBotToken(args[0])
                    .addListener(this)
                    .setEnableShutdownHook(true)
                    .buildBlocking();

        jda.getAccountManager().setGame("devathon.org");

        guild = Validate.notNull(jda.getGuildById(args[1]), "We're not apart of a guild by that ID.");

        channel = guild.getTextChannels()
                       .stream()
                       .filter(channel -> channel.getName().equals(args[2]))
                       .findFirst()
                       .orElseThrow(() -> new NullPointerException("We couldn't find a channel matching " + args[2]));

        contestantRole = guild.getRolesByName(args[3])
                              .stream()
                              .findFirst()
                              .orElseThrow(() -> new NullPointerException("We couldn't find a role matching " + args[3]));

        logger.info("Listening on channel by the name of '" + channel.getName() + "'");
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        final String _message = event.getMessage().getContent();

        if (!event.getChannel().equals(channel) || _message.charAt(0) != COMMAND_PREFIX)
            return;

        final User _sentBy = event.getAuthor();
        final String[] _split = _message.substring(1).split(" ");

        switch ( _split[0].toLowerCase() )
        {
            case "role":
            {
                if (guild.getRolesForUser(_sentBy).contains(contestantRole))
                {
                    mention(_sentBy, "you're already a participant.");
                    return;
                }

                checker.check(_split.length >= 2 ? _split[1] : _sentBy.getUsername(), exists ->
                {
                    if (exists)
                    {
                        guild.getManager().addRoleToUser(_sentBy, contestantRole).update();
                        mention(_sentBy, "thanks for participating! Your role has been applied.");

                        logger.info("Applied [" + contestantRole.getName() + "] to " + _sentBy.getUsername() + "#" + _sentBy.getDiscriminator());
                    }
                    else
                        mention(_sentBy, "you don't seem to be apart of Devathon. Please rerun this command w/ the correct username, or use `!register`.");
                });

                break;
            }

            case "register":
            {
                mention(_sentBy, "please visit https://devathon.org/register to join.");
                break;
            }

            case "until":
            {
                mention(_sentBy, "we have " + LocalDateTime.now().until(STARTING_AT, ChronoUnit.DAYS) + " days left till we start!");
                break;
            }
        }
    }

    /**
     * send a message to the channel already
     * preset to mention the provided {@link User}.
     *
     * @param user the user to mention
     * @param message the content of the
     *                remaining message
     */
    private void mention(User user, String message)
    {
        channel.sendMessage(user.getAsMention() + ", " + message);
    }

    /**
     * setup the JDA logging framework
     * thing to output to a file
     */
    private SimpleLog setupLogger() throws Exception
    {
        final File _file = new File("devathon.log");

        if (!_file.exists())
            _file.createNewFile();

        SimpleLog.addFileLogs(_file, _file);

        return SimpleLog.getLog("Devathon");
    }

    /** app entry */
    public static void main(String[] args) throws Exception
    {
        new DevathonTagApplier(args);
    }

}
