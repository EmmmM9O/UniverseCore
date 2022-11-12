package universecore.components.blockcomp;

import arc.func.Boolf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.gen.Building;
import universecore.annotations.Annotations;

/**此接口给予实现者从一堆元素中依次取出（或者预测取出）元素的功能
 *
 * @author EBwilson
 * @since 1.2*/
public interface Takeable extends BuildCompBase{
  @Annotations.BindField(value = "heaps", initialize = "new arc.struct.ObjectMap()")
  default ObjectMap<String, Heaps<?>> heaps(){
    return null;
  }
  
  /**添加一个输出元素堆，用一个字符串作为名字和一个Seq容器初始化，这个操作不是绝对必要的
   *
   * @param name 堆命名，用于索引
   * @param targets 所有堆元素，选取操作在这当中进行*/
  default void addHeap(String name, Seq<?> targets){
    heaps().put(name, new Heaps<>(targets));
  }
  
  /**添加一个输出元素堆，用一个字符串作为名字和一个Seq容器，以及一个返回布尔值的过滤器函数初始化，这个操作不是绝对必要的
   *
   * @param name 堆命名，用于索引
   * @param targets 所有堆元素，选取操作在这当中进行
   * @param valid 选择过滤器*/
  default <T> void addHeap(String name, Seq<T> targets, Boolf<T> valid){
    heaps().put(name, new Heaps<>(targets, valid));
  }
  
  @SuppressWarnings("unchecked")
  default <T> Heaps<T> getHeaps(String name){
    return (Heaps<T>) heaps().get(name);
  }
  
  default Building getNext(String name){
    return getNext(name, true);
  }
  
  default <T> T getNext(String name, Seq<T> targets){
    return getNext(name, targets, true);
  }
  
  default Building getNext(String name, Boolf<Building> valid){
    return getNext(name, valid, true);
  }
  
  default <T> T getNext(String name, Seq<T> targets, Boolf<T> valid){
    return getNext(name, targets, valid, true);
  }
  
  @SuppressWarnings("unchecked")
  default Building getNext(String name, boolean increase){
    Heaps<Building> heaps;
    if((heaps = (Heaps<Building>) heaps().get(name)) == null){
      heaps = new Heaps<>(getBuilding().proximity);
      heaps().put(name, heaps);
    }
    return increase? heaps.next(): heaps.nextOnly();
  }
  
  @SuppressWarnings("unchecked")
  default Building getNext(String name, Boolf<Building> valid, boolean increase){
    Heaps<Building> heaps;
    if((heaps = (Heaps<Building>) heaps().get(name)) == null){
      heaps = new Heaps<>(getBuilding().proximity, valid);
      heaps().put(name, heaps);
    }
    return increase? heaps.next(valid): heaps.nextOnly(valid);
  }
  
  @SuppressWarnings("unchecked")
  default <T> T getNext(String name, Seq<T> targets, boolean increase){
    Heaps<T> heaps;
    if((heaps = (Heaps<T>) heaps().get(name)) == null){
      heaps = new Heaps<>(targets);
      heaps().put(name, heaps);
    }
    return increase? heaps.next(targets): heaps.nextOnly(targets);
  }
  
  @SuppressWarnings("unchecked")
  default <T> T getNext(String name, Seq<T> targets, Boolf<T> valid, boolean increase){
    Heaps<T> heaps;
    if((heaps = (Heaps<T>) heaps().get(name)) == null){
      heaps = new Heaps<>(targets, valid);
      heaps().put(name, heaps);
    }
    return increase? heaps.next(targets, valid): heaps.nextOnly(targets, valid);
  }
  
  class Heaps<Type>{
    public Seq<Type> targets = new Seq<>();
    public Boolf<Type> valid = e -> true;
    public int heapCounter;
    
    public Heaps(){}
    
    public Heaps(Seq<Type> defaultAll){
      this.targets = defaultAll;
    }
  
    public Heaps(Seq<Type> targets, Boolf<Type> valid){
      this.targets = targets;
      this.valid = valid;
    }
  
    public int increaseCount(int size){
      heapCounter = (heapCounter + 1)%size;
      return heapCounter;
    }
    
    public void setTargets(Seq<Type> other){
      targets = other;
    }
    
    public void setValid(Boolf<Type> other){
      valid = other;
    }
    
    public Type next(){
      return next(targets, valid);
    }
    
    public Type nextOnly(){
      return nextOnly(targets, valid);
    }
    
    public Type next(Boolf<Type> valid){
      return next(targets, valid);
    }
    
    public Type nextOnly(Boolf<Type> valid){
      return nextOnly(targets, valid);
    }
    
    public Type next(Seq<Type> targets){
      return next(targets, valid);
    }
    
    public Type nextOnly(Seq<Type> targets){
      return nextOnly(targets, valid);
    }
    
    public Type next(Seq<Type> targets, Boolf<Type> valid){
      int size = targets.size;
      if(size == 0) return null;
      Type result;
      for(Type ignored : targets){
        result = targets.get(increaseCount(size));
        if(valid.get(result)) return result;
      }
      return null;
    }
    
    public Type nextOnly(Seq<Type> targets, Boolf<Type> valid){
      int size = targets.size, curr = heapCounter;
      if(size == 0) return null;
      Type result;
      for(Type ignored : targets){
        curr = (curr + 1)%size;
        result = targets.get(curr);
        if(valid.get(result)) return result;
      }
      return null;
    }
  }
}
