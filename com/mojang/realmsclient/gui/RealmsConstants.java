package com.mojang.realmsclient.gui;

import java.util.Arrays;
import java.util.List;

public class RealmsConstants {
   public static final String BACKGROUND_LOCATION = "textures/gui/options_background.png";
   public static final String STATS_ICON_LOCATION = "textures/gui/container/stats_icons.png";
   public static final String GUI_ICONS_LOCATION = "textures/gui/icons.png";
   public static final String WORLD_TEMPLATE_IMAGES_LOCATION = "realms:textures/gui/realms/world_template_icons/$ID.png";
   public static final String WORLD_TEMPLATE_DEFAULT_IMAGE_LOCATION = "textures/gui/presets/isles.png";
   public static final List<Integer> WORLD_TEMPLATE_IMAGE_IDS = Arrays.asList(6, 8, 9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
   public static final List<Integer> RANDOM_MINIGAME_IMAGES = Arrays.asList(8, 11, 12, 13, 14, 15, 19, 20);
   public static final int TITLE_HEIGHT = 17;
   public static final int COMPONENT_HEIGHT = 20;
   public static final int COLOR_WHITE = 16777215;
   public static final int COLOR_GRAY = 10526880;
   public static final int COLOR_DARK_GRAY = 5000268;
   public static final int COLOR_MEDIUM_GRAY = 7105644;
   public static final int COLOR_GREEN = 8388479;
   public static final int COLOR_RED = 16711680;
   public static final int COLOR_BLACK = -1073741824;
   public static final int COLOR_YELLOW = 13413468;
   public static final int COLOR_BRIGHT_YELLOW = -256;
   public static final int COLOR_LINK = 3368635;
   public static final int COLOR_LINK_HOVER = 7107012;
   public static final int COLOR_INFO = 8226750;

   public static int row(int i) {
      return 40 + i * 13;
   }

   public static String getMinigameImage(int i) {
      return WORLD_TEMPLATE_IMAGE_IDS.contains(i)
         ? "realms:textures/gui/realms/world_template_icons/$ID.png".replace("$ID", String.valueOf(i))
         : "textures/gui/presets/isles.png";
   }
}
