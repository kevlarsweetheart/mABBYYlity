package icebear.easydocs;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DocsListAdapter extends ArrayAdapter<DocumentContent>{

    public DocsListAdapter(@NonNull Context context, ArrayList<DocumentContent> content) {
        super(context, R.layout.docs_item, content);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View docsView = inflater.inflate(R.layout.docs_item, parent, false);

        DocumentContent singleDocItem = getItem(position);
        TextView nameText = (TextView) docsView.findViewById(R.id.filename);
        TextView typeText = (TextView) docsView.findViewById(R.id.filetype);
        TextView dateTimeText = (TextView) docsView.findViewById(R.id.date_time);

        nameText.setText(singleDocItem.getFileName());
        typeText.setText(singleDocItem.getFileType() + "\n(" +
                String.valueOf(singleDocItem.getImgsCount()) + ") imgs");
        dateTimeText.setText(singleDocItem.getUpdateDateTime());
        return docsView;
    }
}