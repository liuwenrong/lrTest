package com.coolyota.logreport.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.coolyota.logreport.R;
import com.coolyota.logreport.model.FileModel;

import java.io.File;
import java.util.List;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/30
 */
public class ManageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//    public class ManageRecyclerViewAdapter extends RecyclerView.Adapter<ManageRecyclerViewAdapter.ManageViewHolder>{

    public CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            FileModel data = (FileModel) buttonView.getTag();
            data.setSelected(isChecked);

        }
    };
    Activity mActivity;
    List<FileModel> mDatas;
    File[] mFileList;

    public ManageRecyclerViewAdapter(Activity mActivity, File[] mFileList) {
        this.mActivity = mActivity;
        this.mFileList = mFileList;
    }

    public ManageRecyclerViewAdapter(Activity mActivity, List<FileModel> mDatas) {
        this.mActivity = mActivity;
        this.mDatas = mDatas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mActivity).inflate(R.layout.item_manage_file, parent, false);
        ManageViewHolder holder = new ManageViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (mDatas != null) {
            FileModel data = mDatas.get(position);
            ManageViewHolder mHolder = (ManageViewHolder) holder;
            mHolder.tvFileName.setText(data.getFileName());
            mHolder.tvTime.setText("最后修改时间:" + data.getFileTime());
            mHolder.tvFileSize.setText(data.getFileSize());
            mHolder.imgHeader.setImageResource(data.isFold() ? R.mipmap.ic_folder_open_black_36dp :
                    R.drawable.ic_file_black_36dp);
            mHolder.checkBoxFile.setChecked(data.isSelected());

            mHolder.checkBoxFile.setTag(data);
            mHolder.checkBoxFile.setOnCheckedChangeListener(onCheckedChangeListener);
        } /*else if (mFileList != null) {
            File file = mFileList[position];
        ManageViewHolder mHolder = (ManageViewHolder) holder;
        mHolder.tvFileName.setText(file.getName());
        mHolder.tvTime.setText( "" + file.lastModified());
        mHolder.tvFileSize.setText(FileUtil.getDataSize(file));
        mHolder.imgHeader.setImageResource(file.isDirectory() ? R.mipmap.ic_folder_open_black_36dp:
                    R.mipmap.ic_folder_open_black_36dp);
        }*/

    }

    @Override
    public int getItemCount() {
//        return mFileList.length;
//        return mFileList.length > 0 ? mFileList.length : 1;
        return mDatas.size();
    }

    class ManageViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileName;
        TextView tvTime;
        TextView tvFileSize;
        ImageView imgHeader;
        CheckBox checkBoxFile;

        public ManageViewHolder(View view) {
            super(view);
            tvFileName = (TextView) view.findViewById(R.id.file_name);
            tvTime = (TextView) view.findViewById(R.id.tv_time);
            tvFileSize = (TextView) view.findViewById(R.id.tv_file_size);
            imgHeader = (ImageView) view.findViewById(R.id.header_img);
            checkBoxFile = (CheckBox) view.findViewById(R.id.check_box_file);
        }
    }

}
