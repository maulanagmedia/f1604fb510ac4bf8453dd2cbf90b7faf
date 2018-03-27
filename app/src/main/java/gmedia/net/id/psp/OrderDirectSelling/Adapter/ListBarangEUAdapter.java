package gmedia.net.id.psp.OrderDirectSelling.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.OrderDirectSelling.DetailInjectPulsa;
import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ItemLength;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListBarangEUAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();
    public static int selectedItem = 0;

    public ListBarangEUAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_list_barang_end, items);
        this.context = context;
        this.items = items;
        this.selectedItem = 0;
    }

    private static class ViewHolder {
        private TextView tvItem;
        private RadioButton rbItem;
    }

    public List<CustomItem> getData(){

        return items;
    }

    public void setSelected(int position){

        selectedItem = position;
        notifyDataSetChanged();
    }

    public void addMoreData(List<CustomItem> moreData){

        items.addAll(moreData);
        notifyDataSetChanged();
    }

    public void addData(CustomItem moreData){

        items.add(moreData);
        notifyDataSetChanged();
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
            convertView = inflater.inflate(R.layout.cv_list_barang_end, null);
            holder.tvItem = (TextView) convertView.findViewById(R.id.tv_item);
            holder.rbItem = (RadioButton) convertView.findViewById(R.id.rb_item);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem.setText(itemSelected.getItem2());
        holder.rbItem.setText(itemSelected.getItem3());

        if(selectedItem == position){

            holder.rbItem.setChecked(true);
            //DetailInjectPulsa.setSelectedItem(itemSelected);
        }else{
            holder.rbItem.setChecked(false);
        }

        holder.rbItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectedItem = position;
                DetailInjectPulsa.setSelectedItem(itemSelected);
                notifyDataSetChanged();
            }
        });

        return convertView;

    }
}
