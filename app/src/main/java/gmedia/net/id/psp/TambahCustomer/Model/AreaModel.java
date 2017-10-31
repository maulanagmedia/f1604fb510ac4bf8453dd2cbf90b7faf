package gmedia.net.id.psp.TambahCustomer.Model;

/**
 * Created by Shinmaul on 10/26/2017.
 */

public class AreaModel {

    private String value, text;

    public AreaModel(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString(){
        return this.text;
    }
}
