package com.planetnoobs.rxjava.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.planetnoobs.rxjava.R;
import com.planetnoobs.rxjava.network.model.Beer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BeerFilterableAdapter extends RecyclerView.Adapter<BeerFilterableAdapter.MyViewHolder> implements Filterable {
    private List<Beer> beerList;
    private List<Beer> beerListFiltered;
    private Context context;
    private BeersAdapterListener listener;

    public BeerFilterableAdapter(Context context, List<Beer> beerList, BeersAdapterListener listener) {
        this.beerList = beerList;
        this.beerListFiltered = beerList;
        this.context = context;
        this.listener = listener;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.beer_list_row, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        Beer beer = beerListFiltered.get(i);

        holder.circularImageView.setBorderColor(getRandomMaterialColor("400"));
        holder.name.setText(beer.getName());
        holder.style.setText(beer.getStyle());
    }

    /**
     * Chooses random color defined in res/array.xml
     */
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = context.getResources().getIdentifier(String.format("mdcolor_%s", typeColor), "array", context.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 21
     **/
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM d");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }

        return "";
    }

    @Override
    public int getItemCount() {
        return beerListFiltered.size();
    }

    public void setSearchResult() {

    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    beerListFiltered = beerList;
                } else {
                    List<Beer> filteredList = new ArrayList<>();
                    for (Beer row : beerList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    beerListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = beerListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                beerListFiltered = (ArrayList<Beer>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface BeersAdapterListener {
        void onBeerSelected(Beer beer);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.style)
        TextView style;
        @BindView(R.id.circularImageView)
        CircularImageView circularImageView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onBeerSelected(beerListFiltered.get(getAdapterPosition()));
                }
            });
        }
    }
}