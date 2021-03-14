package cn.com.sandi.genericdb.domain;



public class TreeNodeAttrDTO<T>{
    private Long id;
    private Long parentId;
    private Integer treeLevel;
    private T node;
    public TreeNodeAttrDTO(Long id, Long parentId, Integer treeLevel, T node){
        this.id = id;
        this.parentId = parentId;
        this.treeLevel = treeLevel;
        this.node = node;
    }
}
