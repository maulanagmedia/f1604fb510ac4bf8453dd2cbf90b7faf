package gmedia.net.id.psp.NavMarketSurveyAOC.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.psp.R;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListPostmatAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListPostmatAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.cv_list_postmat, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private EditText edtText1;
        private ImageView ivDelete;
    }

    public void addItem(CustomItem item){

        items.add(item);
        notifyDataSetChanged();
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
            convertView = inflater.inflate(R.layout.cv_list_postmat, null);
            holder.edtText1 = (EditText) convertView.findViewById(R.id.edt_text1);
            holder.ivDelete = (ImageView) convertView.findViewById(R.id.iv_delete);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);

        holder.edtText1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                items.get(position).setItem1(editable.toString());
            }
        });

        holder.edtText1.setText(itemSelected.getItem1());

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Konfirmasi")
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("Apakah anda yakin ingin menghapus data?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                items.remove(position);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });

        return convertView;

    }
}
