package edu.bahhadj.tam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.bahhadj.tam.R;
import edu.bahhadj.tam.models.ActiveUserDataObj;

/**
 * Created by COMP on 5/3/2016.
 */
public class ActiveUserAdapter extends BaseAdapter {

    ArrayList<ActiveUserDataObj> lisOfUser;

    public ActiveUserAdapter(ArrayList<ActiveUserDataObj> lisOfUser) {
        this.lisOfUser = lisOfUser;
    }

    public void addUser(int index, ActiveUserDataObj mDataObj) {
        lisOfUser.add(index, mDataObj);
    }


    public void removeUser(int position) {
        lisOfUser.remove(position);
    }

    @Override
    public int getCount() {
        return lisOfUser.size();
    }

    @Override
    public Object getItem(int position) {
        return lisOfUser.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_active_user, parent, false);
            ViewHolder vh = new ViewHolder();
            vh.tvName = (TextView) convertView.findViewById(R.id.tvUserName);
            vh.tvStrength = (TextView) convertView.findViewById(R.id.tvSignalPower);
            convertView.setTag(vh);
        }
        ViewHolder vh = (ViewHolder) convertView.getTag();
        vh.tvName.setText(lisOfUser.get(position).getName());
        vh.tvStrength.setText(lisOfUser.get(position).getSigStrength());
        return convertView;
    }


    private class ViewHolder{
        TextView tvName;
        TextView tvStrength;
    }
}
