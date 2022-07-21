package com.mojang.realmsclient.gui;

import net.minecraft.realms.RealmsScreen;

public abstract class ScreenWithCallback<T> extends RealmsScreen {
   abstract void callback(T var1);
}
