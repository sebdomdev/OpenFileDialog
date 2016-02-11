package de.mxapplications.openfiledialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * Dialog that shows the content of the phones memory (and the SD card) and lets the user choose a file or folder, depending on if {@link #setFolderSelectable(boolean)} has been called with the argument "true" or not.
 * <p>Here is a simple example of how ot use OpenFolderDialog:
 *
 *     <pre>
 *         new OpenFileDialog(getContext()).setOnCloseListener(new OpenFileDialog.OnCloseListener(){
 *             public void onCancel(){}
 *             public void onOk(selectedFile){
 *                 Log.i(TAG, "selected file=" + selectedFile);
 *             }
 *         })
 *         .show();
 *     </pre>
 *
 * </p>
 * Created by MxP on 10/27/2015.
 */
public class OpenFileDialog extends Dialog {
    protected final String PARENT_FOLDER = "..";

    //View references
    private TextView mTitleTextView = null;
    private TextView mPathTextView = null;
    private RecyclerView mFileListView = null;
    private Button mOkButton = null;
    private Button mCancelButton = null;
//    private FileListAdapter mFileListAdapter = null;
    private FileListRecyclerViewAdapter mFileListAdapter = null;

    //Resource IDs
    private int mFolderIcon=R.drawable.ic_folder_black_48dp;
    private int mFileIcon=R.drawable.ic_insert_drive_file_black_48dp;
    private int mFolderUpIcon=R.drawable.ic_keyboard_backspace_black_48dp;
    private int mFileSelectedBackgroundColor;
    private int mFileSelectedColor;

    //Settings for OpenFileDialog
    private boolean mFolderSelectable=false;
    private String mPath;
    private FileItem mSelectedFile;
    private String mTitle;
    private String mOkButtonText;
    private String mCancelButtonText;

    private OnCloseListener mOnCloseListener;

    /***
     * Constructs an OpenFileDialog object with default settings.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     */
    public OpenFileDialog(Context context) {
        super(context);
        init();
    }

    /***
     * Constructs an OpenFileDialog object with default settings and a custom cancel listener.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param cancelListener The listener that's being called when the dialog is cancelled.
     */
    public OpenFileDialog(Context context, OnCancelListener cancelListener) {
        super(context, true, cancelListener);
        init();
    }

    /***
     * Constructs an OpenFileDialog object with default settings.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param themeResId a style resource describing the theme to use for the
     *                   window, or {@code 0} to use the default dialog theme
     *
     */
    public OpenFileDialog(Context context, int themeResId) {
        super(context, themeResId);
        init();
    }

    /***
     * Initialize some basic values.
     */
    private void init(){
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        mFileSelectedBackgroundColor = typedValue.data;

        mFileSelectedColor = Color.WHITE;

        setPath(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(createView());

        //Fix the size of the dialog, so it doesn't change height depending on how many children are in the currently displayed folder.
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(this.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        this.getWindow().setAttributes(layoutParams);
    }

    private View createView(){
        //Create the basic layout (LinearLayout)
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);

        //Create the title and hide it if necessary so that it can be displayed if setTitle(String title) is called
        mTitleTextView = new TextView(getContext());
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTitleTextView.setLayoutParams(linearLayoutParams);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTitleTextView.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_DialogWindowTitle);
        }else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mTitleTextView.setTextAppearance(getContext(), android.R.style.TextAppearance_DeviceDefault_DialogWindowTitle);
        }else{
            mTitleTextView.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
            mTitleTextView.setTypeface(null, Typeface.BOLD);
        }

        int padding = convertDpToPixels(16);
        mTitleTextView.setPadding(padding, padding, padding, padding);
        mTitleTextView.setText(mTitle);
        layout.addView(mTitleTextView);
        if(mTitle ==null){
            mTitleTextView.setVisibility(View.GONE);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        //Create the TextView showing the path
        mPathTextView = new TextView(getContext());
        linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPathTextView.setTextAppearance(android.R.style.TextAppearance_Medium);
        }else {
            mPathTextView.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
        }
        mPathTextView.setLayoutParams(linearLayoutParams);
        padding = convertDpToPixels(4);
        mPathTextView.setPadding(padding, padding, 0, 0);
        mPathTextView.setText(mPath);
        layout.addView(mPathTextView);

        //Create the ListView for the children of the current folder
        mFileListView = new RecyclerView(getContext());
        linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        mFileListView.setLayoutParams(linearLayoutParams);
        layout.addView(mFileListView);
        mFileListView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mFileListAdapter = new FileListRecyclerViewAdapter();
        mFileListView.setAdapter(mFileListAdapter);


        //Create the layout for the OK and Cancel button
        LinearLayout buttonLayout = new LinearLayout(getContext());
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayout.setLayoutParams(linearLayoutParams);
        layout.addView(buttonLayout);

        //Create the OK button
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
            mOkButton = new Button(getContext(), null, android.R.attr.borderlessButtonStyle);
        }else {
            mOkButton = new Button(getContext());
        }
        linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        mOkButton.setLayoutParams(linearLayoutParams);
        if(mOkButtonText==null) {
            mOkButton.setText(android.R.string.ok);
        }else{
            mOkButton.setText(mOkButtonText);
        }
        mOkButton.setEnabled(mFolderSelectable);
        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenFileDialog.this.dismiss();
                if (mOnCloseListener != null) {
                    mOnCloseListener.onOk(getSelectedFile());
                }
            }
        });
        buttonLayout.addView(mOkButton);

        //Create the cancel button
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
            mCancelButton = new Button(getContext(), null, android.R.attr.borderlessButtonStyle);
        }else{
            mCancelButton = new Button(getContext());
        }
        linearLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        mCancelButton.setLayoutParams(linearLayoutParams);
        if(mCancelButtonText==null) {
            mCancelButton.setText(android.R.string.cancel);
        }else{
            mCancelButton.setText(mCancelButtonText);
        }
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedFile = null;
                OpenFileDialog.this.cancel();
                if (mOnCloseListener != null) {
                    mOnCloseListener.onCancel();
                }
            }
        });
        buttonLayout.addView(mCancelButton);

        return layout;
    }

    /***
     * Returns the file the user selected.
     * @return The file the user selected, or null if no file was selected
     */
    public String getSelectedFile(){
        if(mFolderSelectable&&mSelectedFile==null){
            return mPath;
        }else{
            return  mSelectedFile.absolutePath;
        }
    }

    /***
     * Set the path to the folder that the file dialog will show to the user when it starts.
     * By default Environment.getExternalStorageDirectory() is being shown.
     * @param path The path to the folder that will be shown.
     * @return The OpenFileDialog object.
     */
    public OpenFileDialog setPath(String path) {
        this.mPath = path;
        if(mFileListAdapter!=null) {
            mPathTextView.setText(path);
            if(!mPath.equals(File.separator))mFileListAdapter.mFileList.add(new FileItem(PARENT_FOLDER, true));
            mFileListAdapter.mFileList.addAll(new FileItem(mPath, true).listChildren());
            mFileListAdapter.notifyDataSetChanged();
        }
        return this;
    }

    /***
     * Returns the OK-button that the dialog shows on the bottom. The {@link #show()} has to have been called before this method returns a value different from null.
     * @return The OK-button
     */
    public Button getOkButton() {
        return mOkButton;
    }

    /***
     * Set the text that the left-hand button will show. By default this is "OK".
     * @param text The text that the button will show
     * @return The OpenFileDialog-object
     */
    public OpenFileDialog setOkButtonText(String text) {
        this.mOkButtonText = text;
        return this;
    }

    /***
     * Returns the Cancel-button that the dialog shows on the bottom. The {@link #show()} has to have been called before this method returns a value different from null.
     * @return The Cancel-button
     */
    public Button getCancelButton() {
        return mCancelButton;
    }

    /***
     * Set the text that the right-hand button will show. By default this is "Cancel".
     * @param text The text that the button will show
     * @return The OpenFileDialog-object
     */
    public OpenFileDialog setCancelButtonText(String text) {
        this.mCancelButtonText = text;
        return this;
    }

    /***
     * Returns the Resource id that references the drawable used to indicate a file in the dialog's list of entries.
     * @return The Resource id that references the drawable used to indicate a file in the dialog's list of entries.
     */
    public int getFileIcon() {
        return mFileIcon;
    }

    /***
     * Sets the icon that indicates a file in the dialog's list of entries in form of a Resource id.
     * @param fileIcon The Resource id of the drawable used to indicate a file in the dialog's list of entries.
     * @return The OpenFileDialog-object
     */
    public OpenFileDialog setFileIcon(int fileIcon) {
        this.mFileIcon = fileIcon;
        return this;
    }

    /***
     * Returns the Resource id that references the drawable used to indicate a folder in the dialog's list of entries.
     * @return The Resource id that references the drawable used to indicate a folder in the dialog's list of entries.
     */
    public int getFolderIcon() {
        return mFolderIcon;
    }

    /***
     * Sets the icon that indicates a folder in the dialog's list of entries in form of a Resource id.
     * @param folderIcon The Resource id of the drawable used to indicate a folder in the dialog's list of entries.
     * @return The OpenFileDialog-object
     */
    public OpenFileDialog setFolderIcon(int folderIcon) {
        this.mFolderIcon = folderIcon;
        return this;
    }

    /***
     * Returns the Resource id that references the drawable used to indicate the up-navigation (i.e. one hierarchy up in the folder-hierarchy) in the dialog's list of entries.
     * @return The Resource id that references the drawable used to indicate the up-navigation (i.e. one hierarchy up in the folder-hierarchy) in the dialog's list of entries.
     */
    public int getFolderUpIcon(){
        return mFolderUpIcon;
    }

    /***
     * Sets the icon that indicates the up-navigation (i.e. one hierarchy up in the folder-hierarchy) in the dialog's list of entries in form of a Resource id.
     * @param folderUpIcon The Resource id of the drawable used to indicate the up-navigation (i.e. one hierarchy up in the folder-hierarchy) in the dialog's list of entries.
     * @return The OpenFileDialog-object
     */
    public OpenFileDialog setFolderUpIcon(int folderUpIcon){
        this.mFolderUpIcon = folderUpIcon;
        return this;
    }

    /***
     * Returns if folders can be selected. If true, folders can be selected instead of files in the dialog.
     * @return The boolean indicating if folders can be selected. If true, folders can be selected instead of files in the dialog.
     */
    public boolean isFolderSelectable() {
        return mFolderSelectable;
    }

    /***
     * Sets if folders can be selected. If the argument is true, folders can be selected instead of files in the dialog.
     * @param folderSelectable The boolean indicating if folders can be selected. If true, folders can be selected instead of files in the dialog.
     * @return The OpenFileDialog-object.
     */
    public OpenFileDialog setFolderSelectable(boolean folderSelectable) {
        this.mFolderSelectable = folderSelectable;
        return this;
    }

    /***
     * Returns the text color that is used for selected files.
     * @return The text color that is used for selected files.
     */
    public int getFileSelectedColor() {
        return mFileSelectedColor;
    }

    /***
     * Sets the text color used for selected files. This should be a color value not a resource id.
     * @param fileSelectedColor The text color used for selected files.
     * @return The OpenFileDialog-object
     */
    public OpenFileDialog setFileSelectedColor(int fileSelectedColor) {
        this.mFileSelectedColor = fileSelectedColor;
        return this;
    }

    /***
     * Returns the background color that is used for selected files.
     * @return The background color that is used for selected files.
     */
    public int getFileSelectedBackgroundColor() {
        return mFileSelectedBackgroundColor;
    }

    /***
     * Sets the background color used for selected files. This should be a color value not a resource id.
     * @param fileSelectedBackgroundColor The text color used for selected files.
     * @return The OpenFileDialog-object
     */
    public OpenFileDialog setFileSelectedBackgroundColor(int fileSelectedBackgroundColor) {
        this.mFileSelectedBackgroundColor = fileSelectedBackgroundColor;
        return this;
    }

    /***
     * Returns the text that will be displayed as the title of the dialog.
     * @return The text that will be displayed as the title of the dialog.
     */
    public String getTitle(){
        return mTitle;
    }

    /***
     * Sets the text that will be displayed as the title of the dialog.
     * @param title The text that will be displayed as the title of the dialog.
     */
    @Override
    public void setTitle(CharSequence title) {
        if(title!=null) {
            mTitle = title.toString();
            if(mTitleTextView!=null){
                mTitleTextView.setText(mTitle);
                mTitleTextView.setVisibility(View.VISIBLE);
            }else{
                requestWindowFeature(Window.FEATURE_NO_TITLE);
            }
        }
    }

    /***
     * Sets the text that will be displayed as the title of the dialog in form of a Resource id.
     * @param titleId The text that will be displayed as the title of the dialog in form of a Resource id.
     */
    @Override
    public void setTitle(int titleId) {
        setTitle(getContext().getString(titleId));
    }

    /***
     * Sets the OnCloseListener that's methods will be called when the dialog is closed by the user.
     * @param onCloseListener The OnCloseListener-object that's methods will be called when the dialog is closed by the user.
     * @return
     */
    public OpenFileDialog setOnCloseListener(OnCloseListener onCloseListener){
        this.mOnCloseListener = onCloseListener;
        return this;
    }

    private static int convertDpToPixels(int dp){
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics()));
    }

    private class FileListRecyclerViewAdapter extends RecyclerView.Adapter<FileListRecyclerViewAdapter.ViewHolder>{
        private List<FileItem> mFileList = new ArrayList<>();

        public FileListRecyclerViewAdapter(){
            if(mPath!=null){
                if(!mPath.equals(File.separator))mFileList.add(new FileItem(PARENT_FOLDER, true));
                mFileList.addAll(new FileItem(mPath, true).listChildren());
            }
        }

        @Override
        public FileListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.open_file_dialog_item_layout, parent, false);
            return new FileListRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final FileListRecyclerViewAdapter.ViewHolder holder, int position) {
            holder.mFileItem = mFileList.get(position);

            holder.mIconImageView.setImageResource(holder.mFileItem.name.equals(PARENT_FOLDER) ? mFolderUpIcon : holder.mFileItem.isDirectory ? mFolderIcon : mFileIcon);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.mIconImageView.setImageAlpha(138);
            }

            holder.mPathTextView.setText(holder.mFileItem.name);

            if(mSelectedFile!=null&&mSelectedFile.absolutePath==holder.mFileItem.absolutePath&&!mFolderSelectable){
                holder.mView.setBackgroundColor(mFileSelectedBackgroundColor);
                holder.mPathTextView.setTextColor(mFileSelectedColor);
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.mFileItem.isDirectory) {
                        mSelectedFile =null;
                        navigate(holder.mFileItem.name);
                        mOkButton.setEnabled(mFolderSelectable);
                    } else {
                        if (mSelectedFile!=null&&holder.mFileItem.absolutePath==mSelectedFile.absolutePath) {
                            mOkButton.setEnabled(false);
                            mSelectedFile=null;
                        } else {
                            mOkButton.setEnabled(true);
                            mSelectedFile = holder.mFileItem;
                        }
                        notifyDataSetChanged();
                    }
                }
            });
        }

        private void navigate(String fileName){
            if(fileName.equals(PARENT_FOLDER)){
                if(mPath.lastIndexOf(File.separator)==0){
                    mPath = File.separator;
                }else {
                    mPath = mPath.substring(0, mPath.lastIndexOf(File.separator));
                }
            }else{
                mPath = mPath + File.separator + fileName;
            }
            mFileList.clear();

            mPathTextView.setText(mPath);

            if(!mPath.equals(File.separator)){
                mFileList.add(new FileItem(PARENT_FOLDER, true));
            }
            mFileList.addAll(new FileItem(mPath, true).listChildren());
            mFileListAdapter.notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mFileList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            public final View mView;
            public final ImageView mIconImageView;
            public final TextView mPathTextView;
            public FileItem mFileItem;
            public ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
                mIconImageView = (ImageView)itemView.findViewById(R.id.open_file_dialog_item_image_view);
                mPathTextView = (TextView)itemView.findViewById(R.id.open_file_dialog_item_text_view);
            }
        }
    }

    private class FileItem{
        FileItem(String absolutePath, boolean isDirectory){
            this.absolutePath = absolutePath;
            this.isDirectory = isDirectory;
            this.name = absolutePath.substring(absolutePath.lastIndexOf(File.separator)+1);
        }
        String absolutePath;
        String name;
        boolean isDirectory;
        List<FileItem> listChildren(){
            List<FileItem> list = new ArrayList<>();
            File parent = new File(absolutePath);
            if(parent.listFiles()==null)return list;
            File[] fileArr =parent.listFiles();
            Arrays.sort(fileArr, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                }
            });
            for(File file:fileArr){
                if(!file.getName().startsWith(".")) {
                    list.add(new FileItem(file.getAbsolutePath(), file.isDirectory()));
                }
            }
            return list;
        }
    }

    /***
     * This interface offers methods that will be called when the user closes the dialog.
     */
    public interface OnCloseListener {
        /***
         * This methods will be called when the user touches the cancel button.
         */
        void onCancel();

        /***
         * This method will be called when the user selects a file/folder and touches the OK button.
         * @param selectedFile The file or folder that the user has selected.
         */
        void onOk(String selectedFile);
    }
}
