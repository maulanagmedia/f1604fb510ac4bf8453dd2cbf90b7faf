package gmedia.net.id.psp.OrderDirectSelling.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.R;
import gmedia.net.id.psp.Utils.ItemLength;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListBalasanInjectAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListBalasanInjectAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_list_balasan, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem0, tvItem1;
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
            convertView = inflater.inflate(R.layout.cv_list_balasan, null);
            holder.tvItem0 = (TextView) convertView.findViewById(R.id.tv_item0);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem0.setText(itemSelected.getItem1());
        holder.tvItem1.setText(itemSelected.getItem2());
        return convertView;

    }
}
