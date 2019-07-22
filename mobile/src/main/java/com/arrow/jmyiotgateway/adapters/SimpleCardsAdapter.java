package com.arrow.jmyiotgateway.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.arrow.jmyiotgateway.cards.BaseCard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;


public class SimpleCardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = SimpleCardsAdapter.class.getName();
    private final PublishSubject<BaseCard> mOnClickSubject = PublishSubject.create();
    private List<BaseCard> mCards;
    private Bundle savedState;
    private Context mContext;
    private List<Pair<BaseCard, Integer>> mCardsToRemove;

    public SimpleCardsAdapter(Bundle savedState) {
        this.savedState = savedState;
        mCards = new ArrayList();
        mCardsToRemove = new ArrayList<>();
    }

    public SimpleCardsAdapter(Bundle savedState, Context context) {
        this(savedState);
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        for (BaseCard card : mCards) {
            if (viewType == card.getDeviceType().ordinal() && !card.isInitialized()) {
                vh = card.getViewHolder(parent, viewType, savedState);
                break;
            }
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final BaseCard card = mCards.get(position);
        if (card != null) {
            mCards.get(position).bind(holder, mContext);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnClickSubject.onNext(card);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        BaseCard card = mCards.get(position);
        int result = -1;
        if (card != null) {
            result = card.getViewType();
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    public BaseCard getCard(int position) {
        return mCards.get(position);
    }

    public Observable<BaseCard> getPositionClicks() {
        return mOnClickSubject.asObservable();
    }

    public void setCards(List<BaseCard> baseCards) {
        this.mCards = baseCards;
        notifyDataSetChanged();
    }

    public void removeCard(BaseCard card, int position) {
        Log.d(TAG, "removeCard, position: " + position);
        mCardsToRemove.add(new Pair<>(card, position));
        mCards.remove(position);
        notifyItemRemoved(position);
    }

    public boolean completeRemovingCard(BaseCard card) {
        Log.d(TAG, "completeRemovingCard");
        Iterator<Pair<BaseCard, Integer>> it = mCardsToRemove.iterator();
        while (it.hasNext()) {
            Pair<BaseCard, Integer> pair = it.next();
            if (pair.first.equals(card)) {
                pair.first.onCardRemoved();
                it.remove();
                return true;
            }
        }
        return false;
    }

    public void discardRemoving(BaseCard card) {
        Log.d(TAG, "discardRemoving");
        Iterator<Pair<BaseCard, Integer>> it = mCardsToRemove.iterator();
        while (it.hasNext()) {
            Pair<BaseCard, Integer> pair = it.next();
            if (pair.first.equals(card)) {
                mCards.add(pair.second, pair.first);
                notifyItemInserted(pair.second);
                notifyDataSetChanged();
                it.remove();
            }
        }
    }

    public void addCard(BaseCard card) {
        if (!mCards.contains(card)) {
            mCards.add(card);
            notifyItemInserted(mCards.size());
            notifyDataSetChanged();
        }
    }

    public void onStart() {
        for (BaseCard card : mCards) {
            card.onStart();
        }
    }

    public void onStop() {
        for (BaseCard card : mCards) {
            card.onStop();
        }
    }

    public void onPause() {
        for (BaseCard card : mCards) {
            card.onPause();
        }
    }

    public void onResume() {
        for (BaseCard card : mCards) {
            card.onResume();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            for (BaseCard card : mCards) {
                card.onCreate(savedInstanceState);
            }
        }
    }

    public void onDestroy() {
        for (BaseCard card : mCards) {
            card.onDestroy();
        }
        mContext = null;
    }

    public void onSaveInstanceState(Bundle outState) {
        for (BaseCard card : mCards) {
            if (card.isInitialized()) {
                card.onSaveInstanceState(outState);
            } else {
                card.onSaveNonInitInstanceState(savedState, outState);
            }
        }
    }

    public void onLowMemory() {
        for (BaseCard card : mCards) {
            card.onLowMemory();
        }
    }

}
