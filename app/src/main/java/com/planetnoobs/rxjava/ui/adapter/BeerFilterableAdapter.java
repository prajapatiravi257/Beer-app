package com.planetnoobs.rxjava.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.planetnoobs.rxjava.R;
import com.planetnoobs.rxjava.network.model.Beer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BeerFilterableAdapter extends RecyclerView.Adapter<BeerFilterableAdapter.MyViewHolder>
        implements Filterable {

    private List<Beer> beerList;
    private List<Beer> beerListFiltered;
    private Context context;
    private BeersAdapterListener listener;

    public BeerFilterableAdapter(
            Context context, List<Beer> beerList, BeersAdapterListener listener) {
        this.beerList = beerList;
        this.beerListFiltered = beerList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView =
                LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.beer_list_row, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        Beer beer = beerListFiltered.get(i);
        holder.name.setText(beer.getName());
        holder.style.setText(beer.getStyle());
        Double abv = Double.parseDouble(TextUtils.isEmpty(beer.getAbv()) ? "0.0" : beer.getAbv());
        holder.abv.setText(String.format(Locale.US, "%s%%", (int) (abv * 100)));
        holder.abv.setBackground(getBackDrawable(holder.abv.getBackground().getCurrent(), (int) (abv * 100)));
        int ounce = (int) Float.parseFloat(beer.getOunces());
        holder.ounces.setText(
                ounce > 1
                        ? String.format(Locale.US, "%d Ounces", ounce)
                        : String.format(Locale.US, "%d Ounce", ounce));
    }

    private GradientDrawable getBackDrawable(Drawable background, int abv) {
        GradientDrawable gd = (GradientDrawable) background;
        int color = Color.parseColor(getAbvColor(abv));
        gd.setColor(color);
        gd.setCornerRadii(new float[]{30, 30, 30, 30, 0, 0, 30, 30});
        gd.setStroke(2, color, 5, 6);
        return gd;
    }

    private String getAbvColor(int abv) {
        switch (abv) {
            case 10:
                return "#FF0000";
            case 9:
                return "#FF3300";
            case 8:
                return "#ff6600";
            case 7:
                return "#ff9900";
            case 6:
                return "#FFCC00";
            case 5:
                return "#FFFF00";
            case 4:
                return "#ccff00";
            case 3:
                return "#99ff00";
            case 2:
                return "#66ff00";
            case 1:
                return "#33ff00";
            default:
                return "#00FF00";
        }
    }

    @Override
    public int getItemCount() {
        return beerListFiltered.size();
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

        @BindView(R.id.ounces)
        TextView ounces;

        @BindView(R.id.abv)
        TextView abv;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listener.onBeerSelected(beerListFiltered.get(getAdapterPosition()));
                        }
                    });
        }
    }
}
