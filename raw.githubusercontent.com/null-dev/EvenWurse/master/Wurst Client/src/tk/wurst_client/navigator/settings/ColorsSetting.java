/*
 * Copyright � 2014 - 2016 | Alexander01998 and contributors
 * All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package tk.wurst_client.navigator.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import tk.wurst_client.navigator.gui.NavigatorFeatureScreen;
import tk.wurst_client.utils.JsonUtils;

import java.awt.*;

public class ColorsSetting implements NavigatorSetting {
    private String name;
    private boolean[] selected;

    public ColorsSetting(String name, boolean[] selected) {
        if (selected.length != 16) {
            throw new IllegalArgumentException(
                    "Length of 'selected' must be 16 but was " + selected.length + " instead.");
        }

        this.name = name;
        this.selected = selected;
    }

    @Override
    public final void addToFeatureScreen(NavigatorFeatureScreen featureScreen) {
        // text
        featureScreen.addText("\n" + name + ":\n\n\n\n\n\n\n");

        // color buttons
        class ColorButton extends NavigatorFeatureScreen.ButtonData {
            public int index;

            public ColorButton(NavigatorFeatureScreen featureScreen, int x, int y, String displayString, int color,
                               int index) {
                featureScreen.super(x, y, 12, 12, displayString, color);

                this.index = index;
                textColor = color;

                updateColor();
            }

            @Override
            public void press() {
                setSelected(index, !selected[index]);
                updateColor();
            }

            public void updateColor() {
                color = new Color(selected[index] ? 0xcccccc : 0x222222);
            }
        }

        // add color buttons
        int x = featureScreen.getMiddleX() - 104;
        int y = 60 + featureScreen.getTextHeight() - 72;
        String[] colorNames = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        int[] colors =
                new int[]{0x000000, 0x0066cc, 0x00cc00, 0x00cccc, 0xcc0000, 0xcc00cc, 0xff8800, 0xaaaaaa, 0x666666,
                        0x0000ff, 0x00ff00, 0x00ffff, 0xff0000, 0xff8888, 0xffff00, 0xffffff};
        ColorButton[] buttons = new ColorButton[selected.length];
        for (int i = 0; i < selected.length; i++) {
            switch (i % 4) {
                case 0:
                    x -= 48;
                    y += 16;
                    break;
                default:
                    x += 16;
                    break;
            }
            ColorButton button = new ColorButton(featureScreen, x, y, colorNames[i], colors[i], i);
            buttons[i] = button;
            featureScreen.addButton(button);
        }

        // all on button
        x += 16;
        y -= 48;
        featureScreen.addButton(featureScreen.new ButtonData(x, y, 48, 12, "All On", 0x404040) {
            @Override
            public void press() {
                for (int i = 0; i < buttons.length; i++) {
                    selected[i] = true;
                    buttons[i].updateColor();
                }
                update();
            }
        });

        // all off button
        y += 16;
        featureScreen.addButton(featureScreen.new ButtonData(x, y, 48, 12, "All Off", 0x404040) {
            @Override
            public void press() {
                for (int i = 0; i < buttons.length; i++) {
                    selected[i] = false;
                    buttons[i].updateColor();
                }
                update();
            }
        });
    }

    public boolean[] getSelected() {
        return selected;
    }

    public void setSelected(int index, boolean selected) {
        this.selected[index] = selected;
        update();
    }

    @Override
    public final void save(JsonObject json) {
        json.add(name, JsonUtils.gson.toJsonTree(selected));
    }

    @Override
    public final void load(JsonObject json) {
        JsonArray jsonColors = json.get(name).getAsJsonArray();
        for (int i = 0; i < selected.length; i++) {
            selected[i] = jsonColors.get(i).getAsBoolean();
        }
    }

    @Override
    public void update() {

    }
}
