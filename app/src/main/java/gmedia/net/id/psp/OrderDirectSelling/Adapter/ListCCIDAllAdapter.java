package gmedia.net.id.psp.OrderDirectSelling.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.OptionItem;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.R;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListCCIDAllAdapter extends ArrayAdapter{

    private Activity context;
    private List<OptionItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListCCIDAllAdapter(Activity context, List<OptionItem> items) {
        super(context, R.layout.cv_list_ccid_with_cb, items);
        this.context = context;
        this.items = new ArrayList<>(items);
    }

    private static class ViewHolder {
        private CheckBox cbItem;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    public List<OptionItem> getItems(){


        List<OptionItem> outputData = new ArrayList<>(items);
        outputData.remove(0);
        return outputData;
    }

    @Override
    public int getViewTypeCount() {

        if (getCount() != 0)
            return getCount();

        return 1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.cv_list_ccid_with_cb, null);
            holder.cbItem = (CheckBox) convertView.findViewById(R.id.cb_item);
            convertView.setTag(holder);
        }else{
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.cv_list_ccid_with_cb, null);
            holder.cbItem = (CheckBox) convertView.findViewById(R.id.cb_item);
            convertView.setTag(holder);
            //holder = (ViewHolder) convertView.getTag();
        }

        final OptionItem itemSelected = items.get(position);

        if(position == 0){

            boolean selectAll = true;
            for(int x = 1; x <  items.size(); x++){
                if(!items.get(x).isSelected()) selectAll = false;
            }

            holder.cbItem.setChecked(selectAll);
        }else{

            if(itemSelected.isSelected()){
                holder.cbItem.setChecked(true);
            }else{
                holder.cbItem.setChecked(false);
            }
        }

        holder.cbItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(position == 0 && itemSelected.getAtt1().equals("0")){

                    for(int x = 1; x <  items.size(); x++){
                        items.get(x).setSelected(b);
                    }

                    notifyDataSetChanged();
                }else{

                    items.get(position).setSelected(b);
                }
            }
        });

        holder.cbItem.setText(itemSelected.getAtt3());
        return convertView;

    }
}
