package gmedia.net.id.psp.StokSales.Adapter;

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

public class DetailTerjualAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public DetailTerjualAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_terjual, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2;
    }

    public void addMoreData(List<CustomItem> moreData){
        items.addAll(moreData);
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
            convertView = inflater.inflate(R.layout.adapter_terjual, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem1());
        holder.tvItem2.setText(itemSelected.getItem2());
        return convertView;

    }
}
