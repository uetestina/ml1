/*
 * AntiCheat for Bukkit.
 * Copyright (C) 2012-2014 AntiCheat Team | http://gravitydevelopment.net
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.dynamicdev.anticheat.command.executors;

import net.dynamicdev.anticheat.AntiCheat;
import net.dynamicdev.anticheat.command.CommandBase;
import net.dynamicdev.anticheat.util.Permission;

import org.bukkit.command.CommandSender;

public class CommandDeveloper extends CommandBase {

    private static final String NAME = "AntiCheat Developer Mode";
    private static final String COMMAND = "developer";
    private static final String USAGE = "anticheat developer";
    private static final Permission PERMISSION = Permission.SYSTEM_DEBUG;
    private static final String[] HELP = {
            GRAY + "Use: " + AQUA + "/anticheat developer" + GRAY + " to turn on developer mode",
    };

    public CommandDeveloper() {
        super(NAME, COMMAND, USAGE, HELP, PERMISSION);
    }

    @Override
    protected void execute(CommandSender cs, String[] args) {
        AntiCheat.setDeveloperMode(!AntiCheat.developerMode());
        cs.sendMessage(GREEN + "Developer mode " + (AntiCheat.developerMode() ? "ON" : "OFF"));
    }
}
