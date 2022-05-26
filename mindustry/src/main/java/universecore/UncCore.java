package universecore;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.files.ZipFi;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.world.Block;
import universecore.override.dialogs.UncDatabaseDialog;
import universecore.ui.styles.UncStyles;
import universecore.util.animate.CellActions;
import universecore.util.aspect.AspectManager;
import universecore.util.handler.CategoryHandler;
import universecore.util.handler.ClassHandler;

/**UniverseCore的mod主类，同时也是调用核心类，这里会保存各种可能会用到的默认实例以及许多必要实例
 * @author EBwilson
 * @since 0.1*/
public class UncCore extends Mod{
  private static final ObjectMap<String, Class<? extends Mod>> referredMods = new ObjectMap<>();
  
  /**此mod内部名称*/
  public static final String coreName = "universe-core";
  
  /**mod项目地址*/
  public static final String coreGithubProject = "https://github.com/EB-wilson/UniverseCore";
  
  /**模组文件夹位置*/
  public static final Fi modDirectory = Core.settings.getDataDirectory().child("mods");
  /**本模组的文件位置*/
  public static final Fi coreFile = getModFile(coreName, false);
  
  static{
    signup(UncCore.class);
  }

  public static ClassHandler classHandler = ImpCore.classes.getHandler(UncCore.class);
  
  /**方块类别处理工具实例*/
  public static CategoryHandler categories = new CategoryHandler();
  
  /**单元格动画控制器实例*/
  public static CellActions cellActions = new CellActions();
  
  /**切面管理器实例*/
  public static AspectManager aspects = AspectManager.getDefault();
  
  public UncCore(){
    if(Tmp.v1.x == 0){
      Log.info("[Universe Core] core loading");
  
      Events.run(EventType.Trigger.update, () -> {
        cellActions.update();
      });
  
      Time.run(0f, () -> {
        Events.on(EventType.UnlockEvent.class, event -> {
          if(event.content instanceof Block){
            categories.handleBlockFrag();
          }
        });
    
        Events.on(EventType.WorldLoadEvent.class, e -> {
          Core.app.post(categories::handleBlockFrag);
        });
      });

      UncStyles.load();
      
      Tmp.v1.set(1, 0);
    }
    else Log.info("[UniverseCore] core was loaded, skip this repeat load");
  }
  
  public static void signup(Class<? extends Mod> modClass){
    String modName = getModName(modClass);
    referredMods.put(modName, modClass);
  }
  
  @Override
  public void init(){
    if(! Vars.net.server()) Vars.ui.database = new UncDatabaseDialog();
  }
  
  public static Fi getModFile(String modName, boolean zip){
    Fi[] modsFiles = modDirectory.list();
    Fi temp = null;
    
    for(Fi file : modsFiles){
      if(file.isDirectory()) continue;
      Fi zipped = new ZipFi(file);
      Fi modManifest = zipped.child("mod.hjson").exists()? zipped.child("mod.hjson"): zipped.child("mod.json");
      if(modManifest.exists()){
        String name = Jval.read(modManifest.readString()).get("name").toString();
        if(name.equals(modName)) temp = zip? zipped: file;
      }
    }
    
    return temp;
  }
  
  public static String getModName(Class<? extends Mod> modMain){
    Fi[] modsFiles = modDirectory.list();
    
    for(Fi file : modsFiles){
      if(file.isDirectory()) continue;
      Fi zipped = new ZipFi(file);
      Fi modManifest = zipped.child("mod.hjson").exists()? zipped.child("mod.hjson"): zipped.child("mod.json");
      if(modManifest.exists()){
        Jval json = Jval.read(modManifest.readString());
        if(!json.has("main")) return null;
        String main = json.get("main").toString();
        if(main.equals(modMain.getName())) return json.get("name").asString();
      }
    }
    
    return null;
  }
}