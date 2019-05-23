package gmedia.net.id.psp.ActKonsinyasi.Adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.ActKonsinyasi.MutasiKonsinyasi.DetailMutasiKonsinyasi;
import gmedia.net.id.psp.R;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListCCIDKonsinyasiAdapter extends ArrayAdapter {

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListCCIDKonsinyasiAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_list_ccid, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2, tvItem3;
        private ImageView ivDelete;
    }

    public void clearData(){

        items.clear();
        notifyDataSetChanged();

    }

    public List<CustomItem> getDataList(){

        return items;
    }

    public void addData(CustomItem item){

        boolean isExist = false;
        for(CustomItem item1: items){
            if(item1.getItem3().equals(item.getItem3())) isExist = true;
        }

        if(!isExist){
            items.add(0, item);
            notifyDataSetChanged();
            ((DetailMutasiKonsinyasi) context).updateTotal();
        }

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.cv_list_ccid, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            holder.ivDelete = (ImageView) convertView.findViewById(R.id.iv_delete);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText((position + 1)+"");
        holder.tvItem2.setText(itemSelected.getItem2() + " ("+ itemSelected.getItem3()+")");
        holder.tvItem3.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemSelected.getItem4())));
        /*holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setIcon(R.mipmap.ic_launcher)
                        .setTitle("Konfirmasi")
                        .setMessage("Apakah anda yakin ingin menghapus " + itemSelected.getItem2())
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                String ccid = itemSelected.getItem2();
                                items.remove(position);
                                notifyDataSetChanged();
                                ((DetailMutasiKonsinyasi) context).updateTotal();
                            }
                        })
                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
            }
        });*/
        return convertView;

    }
}
