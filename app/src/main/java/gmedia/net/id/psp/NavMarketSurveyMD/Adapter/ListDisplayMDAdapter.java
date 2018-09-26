package gmedia.net.id.psp.NavMarketSurveyMD.Adapter;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ItemLength;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListDisplayMDAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListDisplayMDAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_display_belanja_md, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvText1;
        private EditText edtText1, edtText2, edtText3, edtText4;
    }

    public List<CustomItem> getItems(){

        return items;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.cv_display_belanja_md, null);
            holder.tvText1 = (TextView) convertView.findViewById(R.id.tv_text1);
            holder.edtText1 = (EditText) convertView.findViewById(R.id.edt_text1);
            holder.edtText2 = (EditText) convertView.findViewById(R.id.edt_text2);
            holder.edtText3 = (EditText) convertView.findViewById(R.id.edt_text3);
            holder.edtText4 = (EditText) convertView.findViewById(R.id.edt_text4);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvText1.setText(itemSelected.getItem2());

        final ViewHolder finalHolder = holder;
        finalHolder.edtText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                /*if(!editable.toString().equals(itemSelected.getItem3()) && !editable.toString().isEmpty()){

                    String cleanString = editable.toString().replaceAll("[,.]", "");
                    finalHolder.edtText1.removeTextChangedListener(this);

                    String formatted = iv.ChangeToCurrencyFormat(cleanString);

                    try {
                        items.get(position).setItem3(cleanString);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    finalHolder.edtText1.setText(formatted);
                    finalHolder.edtText1.setSelection(formatted.length());

                    finalHolder.edtText1.addTextChangedListener(this);
                }*/

                try {
                    items.get(position).setItem3(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        finalHolder.edtText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                /*if(!editable.toString().equals(itemSelected.getItem4()) && !editable.toString().isEmpty()){

                    String cleanString = editable.toString().replaceAll("[,.]", "");
                    finalHolder.edtText2.removeTextChangedListener(this);

                    String formatted = iv.ChangeToCurrencyFormat(cleanString);

                    try {
                        items.get(position).setItem4(cleanString);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    finalHolder.edtText2.setText(formatted);
                    finalHolder.edtText2.setSelection(formatted.length());

                    finalHolder.edtText2.addTextChangedListener(this);
                }*/
                try {
                    items.get(position).setItem4(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        finalHolder.edtText3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                /*if(!editable.toString().equals(itemSelected.getItem6()) && !editable.toString().isEmpty()){

                    String cleanString = editable.toString().replaceAll("[,.]", "");
                    finalHolder.edtText3.removeTextChangedListener(this);

                    String formatted = iv.ChangeToCurrencyFormat(cleanString);

                    try {
                        items.get(position).setItem6(cleanString);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    finalHolder.edtText3.setText(formatted);
                    finalHolder.edtText3.setSelection(formatted.length());

                    finalHolder.edtText3.addTextChangedListener(this);
                }*/
                try {
                    items.get(position).setItem6(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        finalHolder.edtText4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                /*if(!editable.toString().equals(itemSelected.getItem6()) && !editable.toString().isEmpty()){

                    String cleanString = editable.toString().replaceAll("[,.]", "");
                    finalHolder.edtText3.removeTextChangedListener(this);

                    String formatted = iv.ChangeToCurrencyFormat(cleanString);

                    try {
                        items.get(position).setItem6(cleanString);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    finalHolder.edtText3.setText(formatted);
                    finalHolder.edtText3.setSelection(formatted.length());

                    finalHolder.edtText3.addTextChangedListener(this);
                }*/
                try {
                    items.get(position).setItem7(editable.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        /*holder.edtText1.setText(iv.ChangeToCurrencyFormat(itemSelected.getItem3()));
        holder.edtText2.setText(iv.ChangeToCurrencyFormat(itemSelected.getItem4()));
        holder.edtText3.setText(iv.ChangeToCurrencyFormat(itemSelected.getItem6()));*/

        holder.edtText1.setText(itemSelected.getItem3());
        holder.edtText2.setText(itemSelected.getItem4());
        holder.edtText3.setText(itemSelected.getItem6());
        holder.edtText4.setText(itemSelected.getItem7());

        return convertView;

    }
}
