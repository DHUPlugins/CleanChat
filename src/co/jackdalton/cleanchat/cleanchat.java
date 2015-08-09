package co.jackdalton.cleanchat;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class cleanchat extends JavaPlugin implements Listener {
    private String version = "0.2.6";
    String customMessage = "Do not use profanity.";
    String ccPrefix = ChatColor.GREEN + "[CleanChat]" + ChatColor.WHITE + " : " + ChatColor.BLUE;
    Boolean useCustomMessages = false;
    Boolean hideLag = true;
    String replacements[] = new String[6];
    Boolean replacementsPopulated = false;
    List<String> toNotify = new ArrayList<>();
    String baseRegex = "\\b(gay|retard|pussy|vagina|penis|(mother|sister)?fuck(er|ing|ed|s)?|shit(er|ing|ed|s|head)?|(dumb|smart)?ass(hole|hat)?|hell|damn(ed|ing)?|cunt|bitch|fag(got)?|(stank|stink)?dick(head|butt)?|cock(head|sucker)?|nigger|nigga|niglet|bastard|sex(y|ier|iest|ing|ed)?|taint|tit(ty|ties|s)?|kabo|h3ll|dam)\\b";
    public void initNotifications() {
        //toNotify.add("Stale_Muffins");
        //toNotify.add("Phoenix_TimeLord");
        //toNotify.add("kylerhagler");
    }
    public void populateReplacements() {
        if (!replacementsPopulated) {
            replacements[0] = "I love DHU so much!";
            replacements[1] = "Guys, let's all go vote for DHU with /vote!";
            replacements[2] = "Stale_Muffins is great at making plugins!";
            replacements[3] = "Aren't kylerhagler and Sparkkles great owners?";
            replacements[4] = "Profanity. Never again.";
            replacements[5] = "CleanChat is my favorite plugin.";
            replacementsPopulated = true;
        }
    }
    public void addWord(String word) {
        String og = this.getConfig().getString("regex");
        String out = og.substring(0, 3) + word + "|" + og.substring(4);
        this.getConfig().set("regex", out);
        this.saveConfig();
    }
    public String getRandomReplacement() {
        populateReplacements();
        return (replacements[new Random().nextInt(replacements.length)]);
    }
    public String getPattern() {
        return this.getConfig().getString("regex");
    }
    public boolean checkOnline(String userName) {
        Player p = Bukkit.getPlayerExact(userName);
        if (p != null) {
            return true;
        }
        return false;
    }
    public void reloadNotifiers() {
        toNotify = this.getConfig().getStringList("toNotify");
    }
    public void saveCfg(String pos, List l) {
        this.getConfig().set(pos, l);
        this.saveConfig();
        reloadNotifiers();
    }
    @Override
    public void onEnable() {
        new ChatListener(this);
        reloadNotifiers();
        //initNotifications();
        this.saveDefaultConfig();
        if (!this.getConfig().contains("useCustomMessages")) {
            this.getConfig().set("useCustomMessages", useCustomMessages);
            this.saveConfig();
        } else {
            updateConfig();
        }
        if (!this.getConfig().contains("hideLag")) {
            this.getConfig().set("hideLag", hideLag);
            this.saveConfig();
        } else {
            updateConfig();
        }
        if (!this.getConfig().contains("regex")) {
            this.getConfig().set("regex", baseRegex);
            this.saveConfig();
        }
    }
    public void notifyAll(String message) {
        reloadNotifiers();
        for (int i = 0; i < toNotify.size(); i++) {
            if (checkOnline(toNotify.get(i))) {
                Player recipient = Bukkit.getPlayerExact(toNotify.get(i));
                recipient.sendMessage(message);
            }
        }
    }
    public void messageSent(PlayerChatEvent event) {
        String m = event.getMessage();
        String pattern = getPattern();
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(m);
        boolean found = matcher.find();
        if (found) {
            notifyAll(ccPrefix + event.getPlayer().getDisplayName() + " said: " + m);
            getLogger().info(ChatColor.RED + "Player " + event.getPlayer().getDisplayName() + " said: " + m);
            if (useCustomMessages) {
                event.setMessage(getRandomReplacement());
            } else {
                event.setCancelled(true);
            }
            event.getPlayer().sendMessage(ChatColor.GREEN + "[CleanChat]" + ChatColor.WHITE + " : " + ChatColor.DARK_RED + customMessage);
        }
        if (hideLag) {
            if (m.equalsIgnoreCase("lag")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("[" + event.getPlayer().getWorld().getName() + "]" + event.getPlayer().getDisplayName() + ChatColor.GRAY + ": " + m);
                notifyAll(ccPrefix + event.getPlayer().getDisplayName() + " said: " + m);
            }
        }
    }
    @Override
    public void onDisable() {
        saveCfg("toNotify", toNotify);
    }
    public void addRecipient(String rec) {
        List og = this.getConfig().getStringList("toNotify");
        og.add(rec);
        saveCfg("toNotify", og);
    }

    public void removeRecipient(String rec, Player sender) {
        List og = this.getConfig().getStringList("toNotify");
        if (og.contains(rec)) {
            int ind = og.indexOf(rec);
            og.remove(ind);
            this.getConfig().set("toNotify", og);
            saveCfg("toNotify", og);
        } else {
            sender.sendMessage(ccPrefix + "Error: " + rec + " is not a valid notification recipient.");
        }
    }
    public void updateConfig() {
        this.getConfig().set("useCustomMessages", useCustomMessages);
        this.getConfig().set("hideLag", hideLag);
        this.saveConfig();
    }
    public void toggle(String cfg, Player p) {
        boolean doSave = false;
        if (cfg.equalsIgnoreCase("usecustommessages")) {
            useCustomMessages = !useCustomMessages;
            p.sendMessage(ccPrefix + "useCustomMessages has been toggled from " + !useCustomMessages + " to " + useCustomMessages + ".");
            doSave = true;
        } else if (cfg.equalsIgnoreCase("hidelag")) {
            hideLag = !hideLag;
            p.sendMessage(ccPrefix + "hideLag has been toggled from " + !hideLag + " to " + hideLag + ".");
            doSave = true;
        } else {
            p.sendMessage(ccPrefix + "Error: " + cfg + " is not a recognized setting.");
        }
        if (doSave) {
            saveConfig();
        }
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("cleanchat")) {
            sender.sendMessage(ccPrefix + "CleanChat v" + version + " | By Jack Dalton // Stale_Muffins");
            sender.sendMessage(ccPrefix + "useCustomMessages is currently set to " + Boolean.toString(useCustomMessages));
            sender.sendMessage(ccPrefix + "hideLag is currently set to " + Boolean.toString(hideLag));
            return true;
        }
        if (command.getName().equalsIgnoreCase("addrecipient")) {
            if (args.length == 1) {
                addRecipient(args[0]);
                sender.sendMessage(ccPrefix + "Recipient \"" + args[0] + "\" added.");
            } else {
                sender.sendMessage(ccPrefix + "Error: 1 argument required; " + args.length + " given.");
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("addword")) {
            if (args.length == 1) {
                addWord(args[0]);
                sender.sendMessage(ccPrefix + args[0] + " added to blacklist.");
            } else {
                sender.sendMessage(ccPrefix + "Error: 1 argument required; " + args.length + " given.");
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("removeword")) {
            if (args.length == 1) {
                sender.sendMessage(ccPrefix + "This feature hasn't been implemented yet. Go complain to jack@jackdalton.co");
                //sender.sendMessage(ccPrefix + args[0] + " removed from blacklist.");
            } else {
                sender.sendMessage(ccPrefix + "Error: 1 argument required; " + args.length + " given.");
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("cctoggle")) {
            if (args.length == 1) {
                toggle(args[0], (Player)sender);
            } else {
                sender.sendMessage(ccPrefix + "Error: 1 argument required; " + args.length + " given.");
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("removerecipient")) {
            if (args.length == 1) {
                removeRecipient(args[0], (Player)sender);
                sender.sendMessage(ccPrefix + "Recipient \"" + args[0] + "\" removed.");
            } else {
                sender.sendMessage(ccPrefix + "Error: 1 argument required; " + args.length + " given.");
            }
            return true;
        }
        if (command.getName().equalsIgnoreCase("listrecipients")) {
            List<String> recipients = getConfig().getStringList("toNotify");
            for (int i = 0; i < recipients.size(); i++) {
                sender.sendMessage(ccPrefix + "[" + i + "] : " + recipients.get(i));
            }
            return true;
        }
        return false;
    }
}