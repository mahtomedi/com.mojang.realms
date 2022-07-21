package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.ErrorCallback;
import com.mojang.realmsclient.gui.LongRunningTask;
import com.mojang.realmsclient.gui.RealmsConstants;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsLongRunningMcoTaskScreen extends RealmsScreen implements ErrorCallback {
   private static final Logger LOGGER = LogManager.getLogger();
   private final int BUTTON_CANCEL_ID = 666;
   private final int BUTTON_BACK_ID = 667;
   private final RealmsScreen lastScreen;
   private final LongRunningTask taskThread;
   private AtomicReference<RealmsScreen> errorScreenToShow = new AtomicReference();
   private volatile String title = "";
   private volatile boolean error;
   private volatile String errorMessage;
   private volatile boolean aborted;
   private int animTicks;
   private final LongRunningTask task;
   private final int buttonLength = 212;
   public static final String[] symbols = new String[]{
      "▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃",
      "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
      "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅",
      "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆",
      "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇",
      "_ _ _ _ _ ▃ ▄ ▅ ▆ ▇ █",
      "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇",
      "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆",
      "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅",
      "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
      "▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃",
      "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _",
      "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _",
      "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
      "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _",
      "█ ▇ ▆ ▅ ▄ ▃ _ _ _ _ _",
      "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _",
      "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
      "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _",
      "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _"
   };

   public void setErrorScreen(RealmsScreen newScreen) {
      this.errorScreenToShow.set(newScreen);
   }

   public RealmsLongRunningMcoTaskScreen(RealmsScreen lastScreen, LongRunningTask task) {
      this.lastScreen = lastScreen;
      this.task = task;
      task.setScreen(this);
      this.taskThread = task;
   }

   public void start() {
      Thread thread = new Thread(this.taskThread, "Realms-long-running-task");
      thread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
      thread.start();
   }

   public void tick() {
      super.tick();
      ++this.animTicks;
      this.task.tick();
   }

   public boolean keyPressed(int eventKey, int scancode, int mods) {
      if (eventKey == 256) {
         this.cancelOrBackButtonClicked();
         return true;
      } else {
         return super.keyPressed(eventKey, scancode, mods);
      }
   }

   public void init() {
      this.task.init();
      this.buttonsAdd(new RealmsButton(666, this.width() / 2 - 106, RealmsConstants.row(12), 212, 20, getLocalizedString("gui.cancel")) {
         public void onClick(double mouseX, double mouseY) {
            RealmsLongRunningMcoTaskScreen.this.cancelOrBackButtonClicked();
         }
      });
   }

   private void cancelOrBackButtonClicked() {
      this.aborted = true;
      this.task.abortTask();
      Realms.setScreen(this.lastScreen);
   }

   public void render(int xm, int ym, float a) {
      RealmsScreen errScr = (RealmsScreen)this.errorScreenToShow.getAndSet(null);
      if (errScr != null) {
         Realms.setScreen(errScr);
      }

      this.renderBackground();
      this.drawCenteredString(this.title, this.width() / 2, RealmsConstants.row(3), 16777215);
      if (!this.error) {
         this.drawCenteredString(symbols[this.animTicks % symbols.length], this.width() / 2, RealmsConstants.row(8), 8421504);
      }

      if (this.error) {
         this.drawCenteredString(this.errorMessage, this.width() / 2, RealmsConstants.row(8), 16711680);
      }

      super.render(xm, ym, a);
   }

   @Override
   public void error(String errorMessage) {
      this.error = true;
      this.errorMessage = errorMessage;
      this.buttonsClear();
      this.buttonsAdd(new RealmsButton(667, this.width() / 2 - 106, this.height() / 4 + 120 + 12, getLocalizedString("gui.back")) {
         public void onClick(double mouseX, double mouseY) {
            RealmsLongRunningMcoTaskScreen.this.cancelOrBackButtonClicked();
         }
      });
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public boolean aborted() {
      return this.aborted;
   }
}
