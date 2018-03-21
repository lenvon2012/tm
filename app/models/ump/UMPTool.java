
package models.ump;

public class UMPTool {

    Long id;

    String toolCode;

    public UMPTool(Long id, String toolCode) {
        super();
        this.id = id;
        this.toolCode = toolCode;
    }

    public static UMPTool getBase() {
        return new UMPTool(2862001L, "policybase001");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToolCode() {
        return toolCode;
    }

    public void setToolCode(String toolCode) {
        this.toolCode = toolCode;
    }

}
