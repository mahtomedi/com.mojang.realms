package com.mojang.realmsclient.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import org.lwjgl.input.Keyboard;

public class BackupLinkScreen extends RealmsScreen {
   private final RealmsScreen lastScreen;
   private final String downloadLink;
   private RealmsEditBox linkBox;
   private String[] desc;
   private RealmsButton copyButton;

   public BackupLinkScreen(RealmsScreen lastScreen, String downloadLink) {
      this.lastScreen = lastScreen;
      this.downloadLink = downloadLink;
   }

   public void tick() {
      this.linkBox.tick();
   }

   public void init() {
      String description = getLocalizedString("mco.backuplink.text");
      List<String> lines = new ArrayList();

      for(String line : this.fontSplit(description, this.width() - 30)) {
         lines.add(line);
      }

      this.desc = (String[])lines.toArray(new String[0]);
      Keyboard.enableRepeatEvents(true);
      this.buttonsClear();
      this.buttonsAdd(this.copyButton = newButton(0, this.width() / 2 - 100, this.height() / 4 + 96 + 12, getLocalizedString("mco.backuplink.copybutton")));
      this.buttonsAdd(newButton(1, this.width() / 2 - 100, this.height() / 4 + 120 + 12, getLocalizedString("gui.back")));
      this.linkBox = this.newEditBox(this.width() / 2 - 100, 116, 200, 20);
      this.linkBox.setMaxLength(500);
      this.linkBox.setFocus(true);
      this.linkBox.setValue(this.downloadLink);
      this.linkBox.setIsEditable(false);
      this.linkBox.moveCursorToStart();
      this.copyToClipboard();
   }

   private void copyToClipboard() {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(new StringSelection(this.downloadLink), null);
   }

   public void buttonClicked(RealmsButton button) {
      if (button.active()) {
         if (button.id() == 1) {
            this.backButtonClicked();
         } else if (button.id() == 0) {
            this.copyToClipboard();
         }

      }
   }

   public void keyPressed(char ch, int eventKey) {
      if (eventKey == 1) {
         this.backButtonClicked();
      } else if (eventKey == 28 || eventKey == 156) {
         this.buttonClicked(this.copyButton);
      }

      this.linkBox.keyPressed(ch, eventKey);
   }

   private void backButtonClicked() {
      Realms.setScreen(this.lastScreen);
   }

   public void mouseClicked(int x, int y, int buttonNum) {
      super.mouseClicked(x, y, buttonNum);
      this.linkBox.mouseClicked(x, y, buttonNum);
   }

   public void render(int xm, int ym, float a) {
      this.renderBackground();
      this.drawCenteredString(getLocalizedString("mco.backuplink.title"), this.width() / 2, 20, 16777215);
      int y = 50;

      for(String line : this.desc) {
         this.drawCenteredString(line, this.width() / 2, y, 8421504);
         y += this.fontLineHeight();
      }

      this.linkBox.render();
      super.render(xm, ym, a);
   }
}
