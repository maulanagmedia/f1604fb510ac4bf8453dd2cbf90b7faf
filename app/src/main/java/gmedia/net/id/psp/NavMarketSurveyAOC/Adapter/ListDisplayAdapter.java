package gmedia.net.id.psp.NavMarketSurveyAOC.Adapter;

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
import gmedia.net.id.psp.Utils.FormatItem;
import gmedia.net.id.psp.Utils.ItemLength;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListDisplayAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListDisplayAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_display_belanja, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvText1;
        private EditText edtText1, edtText2;
    }

    public List<CustomItem> getItems(){

        return items;
    }

    @Override
    public int getCount() {
        if(items.size() < ItemLength.ListLength){
            return super.getCount();
        }else{
            return ItemLength.ListLength;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.cv_display_belanja, null);
            holder.tvText1 = (TextView) convertView.findViewById(R.id.tv_text1);
            holder.edtText1 = (EditText) convertView.findViewById(R.id.edt_text1);
            holder.edtText2 = (EditText) convertView.findViewById(R.id.edt_text2);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvText1.setText(itemSelected.getItem2());

        holder.edtText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                items.get(position).setItem3(editable.toString());
            }
        });

        holder.edtText2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                items.get(position).setItem4(editable.toString());
            }
        });

        holder.edtText1.setText(itemSelected.getItem3());
        holder.edtText2.setText(itemSelected.getItem4());

        return convertView;

    }
}
