package com.mojang.realmsclient.util;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.util.UUIDTypeAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsScreen;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;

public class RealmsTextureManager {
   private static final Map<String, RealmsTextureManager.RealmsTexture> textures = new HashMap();
   private static final Map<String, Boolean> skinFetchStatus = new HashMap();
   private static final Map<String, String> fetchedSkins = new HashMap();
   private static Boolean useMultitextureArb;
   public static int GL_TEXTURE0 = -1;
   private static final Logger LOGGER = LogManager.getLogger();
   private static final String STEVE_LOCATION = "minecraft:textures/entity/steve.png";
   private static final String ALEX_LOCATION = "minecraft:textures/entity/alex.png";

   public static void bindWorldTemplate(String id, String image) {
      if (image == null) {
         RealmsScreen.bind("textures/gui/presets/isles.png");
      } else {
         int textureId = getTextureId(id, image);
         GL11.glBindTexture(3553, textureId);
      }
   }

   public static void bindDefaultFace(UUID uuid) {
      RealmsScreen.bind((uuid.hashCode() & 1) == 1 ? "minecraft:textures/entity/alex.png" : "minecraft:textures/entity/steve.png");
   }

   public static void bindFace(final String uuid) {
      UUID actualUuid = UUIDTypeAdapter.fromString(uuid);
      if (textures.containsKey(uuid)) {
         GL11.glBindTexture(3553, ((RealmsTextureManager.RealmsTexture)textures.get(uuid)).textureId);
      } else if (skinFetchStatus.containsKey(uuid)) {
         if (!skinFetchStatus.get(uuid)) {
            bindDefaultFace(actualUuid);
         } else if (fetchedSkins.containsKey(uuid)) {
            int textureId = getTextureId(uuid, (String)fetchedSkins.get(uuid));
            GL11.glBindTexture(3553, textureId);
         } else {
            bindDefaultFace(actualUuid);
         }

      } else {
         skinFetchStatus.put(uuid, false);
         bindDefaultFace(actualUuid);
         Thread thread = new Thread("Realms Texture Downloader") {
            public void run() {
               Map<Type, MinecraftProfileTexture> fetchedTextures = RealmsUtil.getTextures(uuid);
               if (fetchedTextures.containsKey(Type.SKIN)) {
                  MinecraftProfileTexture textureInfo = (MinecraftProfileTexture)fetchedTextures.get(Type.SKIN);
                  String url = textureInfo.getUrl();
                  HttpURLConnection connection = null;
                  RealmsTextureManager.LOGGER.debug("Downloading http texture from {}", new Object[]{url});

                  try {
                     connection = (HttpURLConnection)new URL(url).openConnection(Realms.getProxy());
                     connection.setDoInput(true);
                     connection.setDoOutput(false);
                     connection.connect();
                     if (connection.getResponseCode() / 100 == 2) {
                        BufferedImage loadedImage;
                        try {
                           loadedImage = ImageIO.read(connection.getInputStream());
                        } catch (Exception var17) {
                           RealmsTextureManager.skinFetchStatus.remove(uuid);
                           return;
                        } finally {
                           IOUtils.closeQuietly(connection.getInputStream());
                        }

                        loadedImage = new SkinProcessor().process(loadedImage);
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        ImageIO.write(loadedImage, "png", output);
                        RealmsTextureManager.fetchedSkins.put(uuid, DatatypeConverter.printBase64Binary(output.toByteArray()));
                        RealmsTextureManager.skinFetchStatus.put(uuid, true);
                        return;
                     }

                     RealmsTextureManager.skinFetchStatus.remove(uuid);
                  } catch (Exception var19) {
                     RealmsTextureManager.LOGGER.error("Couldn't download http texture", var19);
                     RealmsTextureManager.skinFetchStatus.remove(uuid);
                     return;
                  } finally {
                     if (connection != null) {
                        connection.disconnect();
                     }

                  }

               } else {
                  RealmsTextureManager.skinFetchStatus.put(uuid, true);
               }
            }
         };
         thread.setDaemon(true);
         thread.start();
      }
   }

   public static int getTextureId(String id, String image) {
      int textureId;
      if (textures.containsKey(id)) {
         RealmsTextureManager.RealmsTexture texture = (RealmsTextureManager.RealmsTexture)textures.get(id);
         if (texture.image.equals(image)) {
            return texture.textureId;
         }

         GL11.glDeleteTextures(texture.textureId);
         textureId = texture.textureId;
      } else {
         textureId = GL11.glGenTextures();
      }

      IntBuffer buf = null;
      int width = 0;
      int height = 0;

      try {
         InputStream in = new ByteArrayInputStream(new Base64().decode(image));

         BufferedImage img;
         try {
            img = ImageIO.read(in);
         } finally {
            IOUtils.closeQuietly(in);
         }

         width = img.getWidth();
         height = img.getHeight();
         int[] data = new int[width * height];
         img.getRGB(0, 0, width, height, data, 0, width);
         buf = ByteBuffer.allocateDirect(4 * width * height).order(ByteOrder.nativeOrder()).asIntBuffer();
         buf.put(data);
         buf.flip();
      } catch (IOException var12) {
         var12.printStackTrace();
      }

      if (GL_TEXTURE0 == -1) {
         if (getUseMultiTextureArb()) {
            GL_TEXTURE0 = 33984;
         } else {
            GL_TEXTURE0 = 33984;
         }
      }

      glActiveTexture(GL_TEXTURE0);
      GL11.glBindTexture(3553, textureId);
      GL11.glTexImage2D(3553, 0, 6408, width, height, 0, 32993, 33639, buf);
      GL11.glTexParameteri(3553, 10242, 10497);
      GL11.glTexParameteri(3553, 10243, 10497);
      GL11.glTexParameteri(3553, 10240, 9728);
      GL11.glTexParameteri(3553, 10241, 9729);
      textures.put(id, new RealmsTextureManager.RealmsTexture(image, textureId));
      return textureId;
   }

   public static void glActiveTexture(int texture) {
      if (getUseMultiTextureArb()) {
         ARBMultitexture.glActiveTextureARB(texture);
      } else {
         GL13.glActiveTexture(texture);
      }

   }

   public static boolean getUseMultiTextureArb() {
      if (useMultitextureArb == null) {
         ContextCapabilities caps = GLContext.getCapabilities();
         useMultitextureArb = caps.GL_ARB_multitexture && !caps.OpenGL13;
      }

      return useMultitextureArb;
   }

   public static class RealmsTexture {
      String image;
      int textureId;

      public RealmsTexture(String image, int textureId) {
         this.image = image;
         this.textureId = textureId;
      }
   }
}
