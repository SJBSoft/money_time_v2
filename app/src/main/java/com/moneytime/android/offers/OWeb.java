package com.moneytime.android.offers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneytime.android.helper.Surf;
import com.squareup.picasso.Picasso;

import com.moneytime.android.R;

import org.mintsoft.mintlib.GetAuth;

import java.util.HashMap;

public class OWeb extends Fragment {
    private Context context;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        View v = inflater.inflate(R.layout.offers_list, container, false);
        if (context == null || getActivity() == null) return v;
        recyclerView = v.findViewById(R.id.offers_list_recyclerView);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        wAdapter adapter = new wAdapter(context);
        recyclerView.setAdapter(adapter);
    }

    private class wAdapter extends RecyclerView.Adapter<wAdapter.ViewHolder> {
        private final LayoutInflater inflater;
        private final String packagename;
        private final String userId;

        wAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
            this.packagename = context.getPackageName();
            this.userId = GetAuth.user(context);
        }

        @NonNull
        @Override
        public wAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.offers_sdk_grid, parent, false);
            return new wAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull wAdapter.ViewHolder holder, int position) {
            HashMap<String, String> data = Offers.hashList.get("offer_web").get(position);
            String name = data.get("name");
            if (name != null) {
                holder.titleView.setText(data.get("title"));
                holder.descView.setText(data.get("desc"));
                Picasso.get().load(data.get("image")).placeholder(R.drawable.anim_loading).into(holder.imageView);
            }

        }

        @Override
        public int getItemCount() {
            return Offers.hashList.get("offer_web").size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView titleView, descView;
            final ImageView imageView;

            ViewHolder(View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.offers_sdk_grid_titleView);
                descView = itemView.findViewById(R.id.offers_sdk_grid_descView);
                imageView = itemView.findViewById(R.id.offers_sdk_grid_imageView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                HashMap<String, String> data = Offers.hashList.get("offer_web").get(getAbsoluteAdapterPosition());
                switch (data.get("type")) {
                    case "sdk":
                        try {
                            Class<?> c = Class.forName(packagename + ".sdkoffers." + data.get("name"));
                            startActivity(new Intent(context, c).putExtra("user", userId).putExtra("info", data));
                        } catch (ClassNotFoundException e) {
                            Toast.makeText(context, context.getString(R.string.class_not_found), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case "cpv":
                        try {
                            int pos = getAbsoluteAdapterPosition();
                            Class<?> c = Class.forName(packagename + ".sdkoffers." + data.get("name"));
                            startActivity(new Intent(context, c).putExtra("user", userId).putExtra("info", data));
                        } catch (ClassNotFoundException e) {
                            Toast.makeText(context, context.getString(R.string.class_not_found), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case "api": {
                        Intent intent = new Intent(context, APIOffers.class);
                        intent.putExtra("id", data.get("id"));
                        intent.putExtra("title", data.get("title"));
                        startActivity(intent);
                        break;
                    }
                    case "web": {
                        String url = data.get("url");
                        if (url == null) return;
                        Intent intent = new Intent(context, Surf.class);
                        intent.putExtra("fullscreen", true);
                        String[] urls = url.split("@@@");
                        if (urls.length > 1) {
                            intent.putExtra("url", urls[0]);
                            intent.putExtra("cred", urls[1]);
                        } else {
                            intent.putExtra("url", url.replace("@@@", ""));
                        }
                        startActivity(intent);
                        break;
                    }
                }
            }
        }
    }
}
