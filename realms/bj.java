package realms;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
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

public class bj {
   private static final Map<String, bj.a> a = new HashMap();
   private static final Map<String, Boolean> b = new HashMap();
   private static final Map<String, String> c = new HashMap();
   private static final Logger d = LogManager.getLogger();

   public static void a(String id, String image) {
      if (image == null) {
         RealmsScreen.bind("textures/gui/presets/isles.png");
      } else {
         int textureId = b(id, image);
         GlStateManager.bindTexture(textureId);
      }
   }

   public static void a(String uuid, Runnable r) {
      GLX.withTextureRestore(() -> {
         a(uuid);
         r.run();
      });
   }

   private static void a(UUID uuid) {
      RealmsScreen.bind((uuid.hashCode() & 1) == 1 ? "minecraft:textures/entity/alex.png" : "minecraft:textures/entity/steve.png");
   }

   private static void a(final String uuid) {
      UUID actualUuid = UUIDTypeAdapter.fromString(uuid);
      if (a.containsKey(uuid)) {
         GlStateManager.bindTexture(((bj.a)a.get(uuid)).b);
      } else if (b.containsKey(uuid)) {
         if (!b.get(uuid)) {
            a(actualUuid);
         } else if (c.containsKey(uuid)) {
            int textureId = b(uuid, (String)c.get(uuid));
            GlStateManager.bindTexture(textureId);
         } else {
            a(actualUuid);
         }

      } else {
         b.put(uuid, false);
         a(actualUuid);
         Thread thread = new Thread("Realms Texture Downloader") {
            public void run() {
               Map<Type, MinecraftProfileTexture> fetchedTextures = bk.b(uuid);
               if (fetchedTextures.containsKey(Type.SKIN)) {
                  MinecraftProfileTexture textureInfo = (MinecraftProfileTexture)fetchedTextures.get(Type.SKIN);
                  String url = textureInfo.getUrl();
                  HttpURLConnection connection = null;
                  bj.d.debug("Downloading http texture from {}", url);

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
                           bj.b.remove(uuid);
                           return;
                        } finally {
                           IOUtils.closeQuietly(connection.getInputStream());
                        }

                        loadedImage = new bl().a(loadedImage);
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        ImageIO.write(loadedImage, "png", output);
                        bj.c.put(uuid, DatatypeConverter.printBase64Binary(output.toByteArray()));
                        bj.b.put(uuid, true);
                        return;
                     }

                     bj.b.remove(uuid);
                  } catch (Exception var19) {
                     bj.d.error("Couldn't download http texture", var19);
                     bj.b.remove(uuid);
                     return;
                  } finally {
                     if (connection != null) {
                        connection.disconnect();
                     }

                  }

               } else {
                  bj.b.put(uuid, true);
               }
            }
         };
         thread.setDaemon(true);
         thread.start();
      }
   }

   private static int b(String id, String image) {
      int textureId;
      if (a.containsKey(id)) {
         bj.a texture = (bj.a)a.get(id);
         if (texture.a.equals(image)) {
            return texture.b;
         }

         GlStateManager.deleteTexture(texture.b);
         textureId = texture.b;
      } else {
         textureId = GlStateManager.genTexture();
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

      GlStateManager.activeTexture(GLX.GL_TEXTURE0);
      GlStateManager.bindTexture(textureId);
      TextureUtil.initTexture(buf, width, height);
      a.put(id, new bj.a(image, textureId));
      return textureId;
   }

   public static class a {
      String a;
      int b;

      public a(String image, int textureId) {
         this.a = image;
         this.b = textureId;
      }
   }
}
