package com.voxelgameslib.voxelgameslib.command.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.voxelgameslib.voxelgameslib.lang.Lang;
import com.voxelgameslib.voxelgameslib.lang.LangHandler;
import com.voxelgameslib.voxelgameslib.lang.LangKey;
import com.voxelgameslib.voxelgameslib.lang.Locale;
import com.voxelgameslib.voxelgameslib.user.User;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Handles all commands related to lang and i18n
 */
@Singleton
@SuppressWarnings("JavaDoc") // commands don't need javadoc, go read the command's descriptions
public class LangCommands extends BaseCommand {

    @Inject
    private LangHandler langHandler;

    @CommandAlias("lang")
    @CommandPermission("%user")
    public void lang(User sender) {
        StringBuilder sb = new StringBuilder();
        for (Locale loc : langHandler.getInstalledLocales()) {
            sb.append(loc.getTag()).append(" (").append(loc.getName()).append("), ");
        }
        sb.setLength(sb.length() - 1);
        Lang.msg(sender, LangKey.LANG_INSTALLED, sb.toString());
        Lang.msg(sender, LangKey.LANG_CURRENT, sender.getLocale().getName());
    }

    @Subcommand("set")
    @CommandPermission("%user")
    @Syntax("<locale> - the new locale you want to use")
    @CommandCompletion("@locales")
    public void set(User sender, Locale locale) {
        sender.setLocale(locale);
        Lang.msg(sender, LangKey.LANG_UPDATE, locale.getName());
        if (!langHandler.getInstalledLocales().contains(locale)) {
            Lang.msg(sender, LangKey.LANG_NOT_ENABLED, locale.getName());
        }

        //TODO force user data save
    }
}
