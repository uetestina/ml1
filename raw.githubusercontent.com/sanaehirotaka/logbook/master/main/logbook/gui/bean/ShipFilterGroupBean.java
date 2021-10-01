package logbook.gui.bean;

import logbook.annotation.Name;

/**
 * グループエディターのBean
 *
 */
public class ShipFilterGroupBean {

    /** ID */
    @Name("ID")
    private Long id;

    /** 艦隊 */
    @Name("艦隊")
    private String fleetid;

    /** 名前 */
    @Name("名前")
    private String name;

    /** 艦種 */
    @Name("艦種")
    private String type;

    /** Lv */
    @Name("Lv")
    private Long lv;

    /** 疲労 */
    @Name("疲労")
    private Long cond;

    /** 出撃海域 */
    @Name("出撃海域")
    private String sallyArea;

    /**
     * IDを取得します。
     * @return ID
     */
    public Long getId() {
        return this.id;
    }

    /**
     * IDを設定します。
     * @param id ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 艦隊を取得します。
     * @return 艦隊
     */
    public String getFleetid() {
        return this.fleetid;
    }

    /**
     * 艦隊を設定します。
     * @param fleetid 艦隊
     */
    public void setFleetid(String fleetid) {
        this.fleetid = fleetid;
    }

    /**
     * 名前を取得します。
     * @return 名前
     */
    public String getName() {
        return this.name;
    }

    /**
     * 名前を設定します。
     * @param name 名前
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 艦種を取得します。
     * @return 艦種
     */
    public String getType() {
        return this.type;
    }

    /**
     * 艦種を設定します。
     * @param type 艦種
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Lvを取得します。
     * @return Lv
     */
    public Long getLv() {
        return this.lv;
    }

    /**
     * Lvを設定します。
     * @param lv Lv
     */
    public void setLv(Long lv) {
        this.lv = lv;
    }

    /**
     * 疲労を取得します。
     * @return 疲労
     */
    public Long getCond() {
        return this.cond;
    }

    /**
     * 疲労を設定します。
     * @param cond 疲労
     */
    public void setCond(Long cond) {
        this.cond = cond;
    }

    /**
     * 出撃海域を取得します。
     * @return 出撃海域
     */
    public String getSallyArea() {
        return this.sallyArea;
    }

    /**
     * 出撃海域を設定します。
     * @param sallyArea 出撃海域
     */
    public void setSallyArea(String sallyArea) {
        this.sallyArea = sallyArea;
    }

}
