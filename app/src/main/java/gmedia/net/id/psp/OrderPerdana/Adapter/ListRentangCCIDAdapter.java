package gmedia.net.id.psp.OrderPerdana.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.OptionItem;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.psp.R;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListRentangCCIDAdapter extends ArrayAdapter{

    private Activity context;
    private List<OptionItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListRentangCCIDAdapter(Activity context, List<OptionItem> items) {
        super(context, R.layout.cv_list_rentang_ccid, items);
        this.context = context;
        this.items = new ArrayList<>(items);
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2;
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.cv_list_rentang_ccid, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final OptionItem itemSelected = items.get(position);

        holder.tvItem1.setText(String.valueOf(position+1));
        holder.tvItem2.setText(itemSelected.getText());
        return convertView;

    }
}
