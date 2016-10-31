package com.outdatedversion;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

/**
 * OutdatedVersion
 * Oct/30/2016 (11:16 PM)
 */

public class DevathonTagApplier extends ListenerAdapter
{

    // https://discordapp.com/api/oauth2/authorize?client_id=242502184195850241&scope=bot&permissions=268438528

    private final char COMMAND_PREFIX = '!';
    private final LocalDateTime STARTING_AT = LocalDateTime.of(2016, Month.NOVEMBER, 5, 12, 0);

    private final JDA jda;
    private final Checker checker;

    private String channelName;
    private Guild guild;
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

        jda = new JDABuilder()
                    .setBotToken(args[0])
                    .addListener(this)
                    .setEnableShutdownHook(true)
                    .buildBlocking();

        jda.getAccountManager().setGame("devathon.org");

        guild = Validate.notNull(jda.getGuildById(args[1]), "We're not apart of a guild by that ID.");

        channelName = args[2];

        contestantRole = guild.getRolesByName(args[3])
                              .stream()
                              .findFirst()
                              .orElseThrow(() -> new NullPointerException("We couldn't find a role matching " + args[3]));
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        final TextChannel _channel = event.getChannel();

        if (!_channel.getName().equals(channelName))
            return;

        final User _sentBy = event.getAuthor();
        final String _message = event.getMessage().getContent();

        if (_message.charAt(0) != COMMAND_PREFIX)
            return;

        final String[] _args = ArrayUtils.remove(_message.substring(1).split(" "), 0);

        switch ( _message.toLowerCase().substring(1).split(" ")[0] )
        {
            case "role":
            {
                checker.check(_args.length >= 1 ? _args[0] : _sentBy.getUsername(), exists ->
                {
                    if (exists)
                    {
                        guild.getManager().addRoleToUser(_sentBy, contestantRole).update();
                        _channel.sendMessage(_sentBy.getAsMention() + ", thanks for participating! Your role has been applied.");
                    }
                    else
                        _channel.sendMessage(_sentBy.getAsMention() + ", you don't seem to be apart of Devathon. Please rerun this command w/ the correct username, or use `!register`.");
                });

                break;
            }

            case "register":
            {
                _channel.sendMessage(_sentBy.getAsMention() + ", please visit https://devathon.org/register to join.");
                break;
            }

            case "until":
            {
                _channel.sendMessage(_sentBy.getAsMention() + ", we have " + LocalDateTime.now().until(STARTING_AT, ChronoUnit.DAYS) + " days left till we start!");
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        new DevathonTagApplier(args);
    }

}
