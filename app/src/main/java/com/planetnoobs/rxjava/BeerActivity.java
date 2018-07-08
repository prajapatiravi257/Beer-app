package com.planetnoobs.rxjava;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;
import com.planetnoobs.rxjava.network.ApiClient;
import com.planetnoobs.rxjava.network.ApiService;
import com.planetnoobs.rxjava.network.model.Beer;
import com.planetnoobs.rxjava.ui.adapter.BeerFilterableAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class BeerActivity extends AppCompatActivity implements BeerFilterableAdapter.BeersAdapterListener {
    private static final String TAG = BeerActivity.class.getSimpleName();

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    BeerFilterableAdapter adapter;
    List<Beer> beerList = new ArrayList<>();

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.cordinator)
    CoordinatorLayout cordinator;
    @BindView(R.id.search)
    TextInputEditText searchInput;

    private ApiService apiService;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_beer);
        ButterKnife.bind(this);

        toolbar.setTitle(getString(R.string.activity_title_home));
        setSupportActionBar(toolbar);

        // white background notification bar
        // whiteNotificationBar(fab);

        apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);
        adapter = new BeerFilterableAdapter(this, beerList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(
                new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(
                        this,
                        recyclerView,
                        new RecyclerTouchListener.ClickListener() {
                            @Override
                            public void onClick(View view, int position) {
                            }

                            @Override
                            public void onLongClick(View view, int position) {
                            }
                        }));

        setupSearch();

        getAllBeers();
    }


    @SuppressLint("CheckResult")
    private void setupSearch() {
        disposable.add(RxTextView.textChangeEvents(searchInput)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .filter(new Predicate<TextViewTextChangeEvent>() {
                    @Override
                    public boolean test(TextViewTextChangeEvent textViewTextChangeEvent) {
                        return TextUtils.isEmpty(textViewTextChangeEvent.text().toString()) || textViewTextChangeEvent.text().toString().length() > 2;
                    }
                })
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(searchBeer()));

    }

    private DisposableObserver<TextViewTextChangeEvent> searchBeer() {
        return new DisposableObserver<TextViewTextChangeEvent>() {
            @Override
            public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
                Log.d(TAG, "Search query: " + textViewTextChangeEvent.text());
                adapter.getFilter().filter(textViewTextChangeEvent.text());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
    }

    @SuppressLint("CheckResult")
    private void getAllBeers() {
        ApiService apiService = ApiClient.getClient(getApplicationContext()).create(ApiService.class);

        final MaterialDialog progress = new MaterialDialog.Builder(this)
                .title("Please Wait")
                .progress(true, 0)
                .build();

        progress.show();
        apiService
                .fetchAllBeer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(
                        new Function<List<Beer>, List<Beer>>() {
                            @Override
                            public List<Beer> apply(List<Beer> beers) {
                                Collections.sort(
                                        beers,
                                        new Comparator<Beer>() {
                                            @Override
                                            public int compare(Beer beer, Beer t1) {
                                                return t1.getId() - beer.getId();
                                            }
                                        });
                                return beers;
                            }
                        })
                .subscribeWith(
                        new DisposableSingleObserver<List<Beer>>() {
                            @Override
                            public void onSuccess(List<Beer> beers) {
                                progress.hide();
                                // Received all beer
                                beerList.clear();
                                beerList.addAll(beers);
                                adapter.notifyDataSetChanged();

                                toggleEmptyBeer();
                            }

                            @Override
                            public void onError(Throwable e) {
                                // Network error
                                progress.hide();
                                Log.e(TAG, "onError: " + e.getMessage());
                                showError(e);
                            }
                        });
    }

    private void toggleEmptyBeer() {
        if (beerList.size() > 0) {
            // set empty state view gone
        } else {

        }
    }

    /**
     * Showing a Snackbar with error message The error body will be in json format {"error": "Error
     * message!"}
     */
    private void showError(Throwable e) {
        String message = "";
        try {
            if (e instanceof IOException) {
                message = "No internet connection!";
            } else if (e instanceof HttpException) {
                HttpException error = (HttpException) e;
                String errorBody = error.response().errorBody().string();
                JSONObject jObj = new JSONObject(errorBody);

                message = jObj.getString("error");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (TextUtils.isEmpty(message)) {
            message = "Unknown error occurred!";
        }

        Snackbar snackbar = Snackbar.make(cordinator, message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }

    @Override
    public void onBeerSelected(Beer beer) {

    }
}
