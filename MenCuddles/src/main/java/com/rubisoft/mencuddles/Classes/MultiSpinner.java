package com.rubisoft.mencuddles.Classes;


import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.widget.SpinnerAdapter;

import com.rubisoft.mencuddles.R;
import com.rubisoft.mencuddles.tools.utils;

public class MultiSpinner extends androidx.appcompat.widget.AppCompatTextView implements DialogInterface.OnMultiChoiceClickListener {

    private SpinnerAdapter mAdapter;
    private boolean[] mOldSelection;
    private boolean[] mSelected;
    private String mDefaultText;
    private String mAllText;
    private boolean mAllSelected;
    private final DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            // all selected by default
            MultiSpinner.this.mOldSelection = new boolean[MultiSpinner.this.mAdapter.getCount()];
            MultiSpinner.this.mSelected = new boolean[MultiSpinner.this.mAdapter.getCount()];
            for (int i = 0; i < MultiSpinner.this.mSelected.length; i++) {
                MultiSpinner.this.mOldSelection[i] = false;
                MultiSpinner.this.mSelected[i] = MultiSpinner.this.mAllSelected;
            }
        }
    };
    private MultiSpinner.MultiSpinnerListener mListener;
    private final OnClickListener onClickListener = v -> {
		try {
			Builder builder = new Builder(MultiSpinner.this.getContext());

			String[] choices = new String[MultiSpinner.this.mAdapter.getCount()];

			for (int i = 0; i < choices.length; i++) {
				choices[i] = MultiSpinner.this.mAdapter.getItem(i).toString();
			}

			System.arraycopy(MultiSpinner.this.mSelected, 0, MultiSpinner.this.mOldSelection, 0, MultiSpinner.this.mSelected.length);

			builder.setMultiChoiceItems(choices, MultiSpinner.this.mSelected, MultiSpinner.this);

			builder.setNegativeButton(R.string.Cancelar, (dialog, which) -> {
				try {
					System.arraycopy(MultiSpinner.this.mOldSelection, 0, MultiSpinner.this.mSelected, 0, MultiSpinner.this.mSelected.length);

					dialog.dismiss();
				} catch (Exception e) {
					utils.registra_error(e.toString(), "setNegativeButton de MultiSpinner");
				}
			});

			builder.setPositiveButton(R.string.ok, (dialog, which) -> {
				try {
					MultiSpinner.this.refreshSpinner();
					//	//MultiSpinner.this.mListener.onItemsSelected(MultiSpinner.this.mSelected);
					dialog.dismiss();
				} catch (Exception e) {
					utils.registra_error(e.toString(), "setPositiveButton de MultiSpinner");
				}
			});
			builder.show();
		}catch (Exception e){
			utils.registra_error(e.toString(), "onClick de MultiSpinner");
		}
	};

    public MultiSpinner(Context context) {
        super(context);
    }

    public MultiSpinner(Context context, AttributeSet attr) {
        this(context, attr, R.attr.spinnerStyle);
    }


    public MultiSpinner(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        this.mSelected[which] = isChecked;
    }

    public SpinnerAdapter getAdapter() {
        return mAdapter;
    }

    public boolean hay_alguno_seleccionado() {
        boolean resultado = false;
        for (boolean aMSelected : mSelected) {
            if (aMSelected) {
                resultado = true;
            }
        }
        return resultado;
    }

    public void setAdapter(SpinnerAdapter adapter, boolean allSelected, MultiSpinner.MultiSpinnerListener listener) {
        SpinnerAdapter oldAdapter = mAdapter;

        this.setOnClickListener(null);

        mAdapter = adapter;
        mListener = listener;
        mAllSelected = allSelected;

        if (oldAdapter != null) {
            oldAdapter.unregisterDataSetObserver(this.dataSetObserver);
        }

        if (this.mAdapter != null) {
            this.mAdapter.registerDataSetObserver(this.dataSetObserver);

            // all selected by default
            this.mOldSelection = new boolean[this.mAdapter.getCount()];
            this.mSelected = new boolean[this.mAdapter.getCount()];
            for (int i = 0; i < this.mSelected.length; i++) {
                this.mOldSelection[i] = false;
                this.mSelected[i] = allSelected;
            }

            this.setOnClickListener(this.onClickListener);
        }

        // all text on the spinner
        this.setText(this.mAllText);
    }

    public void setOnItemsSelectedListener(MultiSpinner.MultiSpinnerListener listener) {
        mListener = listener;
    }

    public boolean[] getSelected() {
        return mSelected;
    }

    public void setSelected(boolean[] selected) {
        if (mSelected.length != selected.length)
            return;

        mSelected = selected;

        this.refreshSpinner();
    }

    private void refreshSpinner() {
        // refresh text on spinner
        StringBuilder spinnerBuffer = new StringBuilder();
        boolean someUnselected = false;
        boolean allUnselected = true;

        for (int i = 0; i < this.mAdapter.getCount(); i++) {
            if (this.mSelected[i]) {
                spinnerBuffer.append(this.mAdapter.getItem(i));
                spinnerBuffer.append(", ");
                allUnselected = false;
            } else {
                someUnselected = true;
            }
        }

        String spinnerText;

        if (allUnselected) {
            spinnerText = this.mDefaultText;
        } else {
            if (someUnselected /*&& !(mAllText != null && mAllText.length() > 0)*/) {
                spinnerText = spinnerBuffer.toString();
                if (spinnerText.length() > 2)
                    spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
            } else {
                spinnerText = this.mAllText;
            }
        }

        this.setText(spinnerText);
    }

    public String getDefaultText() {
        return this.mDefaultText;
    }

    public void setDefaultText(String defaultText) {
        mDefaultText = defaultText;
    }

    public String getAllText() {
        return this.mAllText;
    }

    public void setAllText(String allText) {
        mAllText = allText;
    }

    private interface MultiSpinnerListener {
        void onItemsSelected(boolean[] selected);
    }
}

