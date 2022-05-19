package universecore.util.classes;

import arc.Core;
import arc.files.Fi;
import arc.files.ZipFi;
import arc.util.Log;
import arc.util.serialization.Jval;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class JarList{
  protected static final Fi jarFileCache = Core.settings.getDataDirectory().child("universecore").child("cache");
  private final static Fi listFile = jarFileCache.child("mod-generatedJars.lis");
  private final HashMap<String, InfoEntry> list = new HashMap<>();
  
  public JarList(){
    jarFileCache.mkdirs();
    if(!listFile.exists()) return;
    BufferedReader reader = new BufferedReader(listFile.reader());
    String line;
    InfoEntry entry;
    try{
      while((line = reader.readLine()) != null){
        line = line.trim();
        if(line.equals("")) continue;
        entry = InfoEntry.infoOf(line);
        list.put(entry.name, entry);
      }
    }catch(IOException e){
      e.printStackTrace();
    }
  }
  
  public boolean matched(ModInfo mod){
    String md5Code = getMd5(mod.file);
    InfoEntry entry = list.get(mod.name);
    if(entry == null) return false;
    if(!mod.version.equals(entry.version)) return false;
    return md5Code.equals(entry.getMd5());
  }
  
  public Fi getCacheFile(ModInfo mod){
    InfoEntry entry = list.get(mod.name);
    if(entry == null){
      Log.info("new mod: " + mod.name + " installed, to generate class cache");
      entry = new InfoEntry();
      entry.name = mod.name;
      entry.version = mod.version;
      entry.file = jarFileCache.child("generated-" + mod.name + ".jar");
      entry.file.delete();
      entry.md5 = getMd5(mod.file);
      list.put(mod.name, entry);
      writeToList();
    }
    else if(!entry.version.equals(mod.version) || !getMd5(mod.file).equals(entry.getMd5())){
      Log.info("source mod: " + mod.name + " is updated, regenerate class cache");
      entry.version = mod.version;
      entry.file.delete();
      entry.md5 = getMd5(mod.file);
      writeToList();
    }
    else Log.info("loading mod: " + mod.name + " class cache");
    return entry.file;
  }
  
  private void writeToList(){
    try(BufferedWriter writer = new BufferedWriter(listFile.writer(false))){
      for(InfoEntry entry : list.values()){
        Log.info(entry);
        writer.write(entry.toString());
        writer.newLine();
      }
    }catch(IOException e){
      throw new RuntimeException(e);
    }
  }
  
  public static class ModInfo{
    public final String name;
    public final String version;
    public final Fi file;
    
    public ModInfo(Fi modFile){
      if(modFile instanceof ZipFi) throw new RuntimeException("need a zip file with non-zip file object");
      file = modFile;
      modFile = new ZipFi(modFile);
      Fi modMeta = modFile.child("mod.json").exists()? modFile.child("mod.json"): modFile.child("mod.hjson");
      if(!modMeta.exists()) throw new RuntimeException("file: " + modFile + " is not a mod file");
      Jval info = Jval.read(modMeta.reader());
      name = info.get("name").asString();
      version = info.get("version").asString();
    }
  }
  
  public static class InfoEntry{
    private String name;
    private String version;
    private String md5;
    private Fi file;
    
    public String getName(){
      return name;
    }
    
    public String getVersion(){
      return version;
    }
    
    public String getMd5(){
      return md5;
    }
    
    public Fi getFile(){
      return file;
    }
    
    public String toString(){
      return "name=" + name + ";version=" + version + ";MD5=" + getMd5() + ";file=" + file.name() + ";";
    }
    
    public static InfoEntry infoOf(String str){
      InfoEntry result = new InfoEntry();
      StringBuilder keyBuffer = new StringBuilder();
      StringBuilder valueBuffer = new StringBuilder();
  
      BufferedReader reader = new BufferedReader(new StringReader(str));
      int character;
      try{
        String c;
        String key;
        String value;
        boolean inValue = false;
        while((character = reader.read()) != - 1){
          c = Character.toString(character);
          if(c.equals(";")){
            if(!inValue) throw new IllegalArgumentException("unexpected \";\"");
            key = keyBuffer.toString();
            value = valueBuffer.toString();
  
            System.out.println(key);
            System.out.println(value);
            switch(key){
              case "name": result.name = value; break;
              case "version": result.version = value; break;
              case "MD5": result.md5 = value; break;
              case "file": result.file = new Fi(jarFileCache.path() + "/" + value); break;
              default: throw new IllegalArgumentException("unknown key: " + key);
            }
            
            inValue = false;
            keyBuffer = new StringBuilder();
            valueBuffer = new StringBuilder();
          }
          else if(c.equals("=")){
            inValue = true;
          }
          else{
            if(inValue){
              valueBuffer.append(c);
            }
            else keyBuffer.append(c);
          }
        }
        reader.close();
      }catch(IOException e){
        throw new RuntimeException(e);
      }
      
      return result;
    }
  }
  
  private static String getMd5(Fi file){
    MessageDigest md;
    byte[] buffer = new byte[8192];
    try{
      md = MessageDigest.getInstance("MD5");
      InputStream input = new FileInputStream(file.file());
      int data;
      while((data = input.read(buffer)) != - 1){
        md.update(buffer, 0, data);
      }
      input.close();
      return new BigInteger(1, md.digest()).toString(16);
    }catch(IOException | NoSuchAlgorithmException e){
      throw new RuntimeException(e);
    }
  }
}