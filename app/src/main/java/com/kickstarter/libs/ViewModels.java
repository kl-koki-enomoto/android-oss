package com.kickstarter.libs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.utils.BundleUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ViewModels {
  private static final String VIEW_MODEL_ID_KEY = "view_model_id";
  private static final String VIEW_MODEL_STATE_KEY = "view_model_state";

  private static final ViewModels instance = new ViewModels();
  private HashMap<String, ViewModel> viewModels = new HashMap<>();

  public static ViewModels getInstance() {
    return instance;
  }

  @SuppressWarnings("unchecked")
  public <T extends ViewModel> T fetch(@NonNull final Context context, @NonNull final Class<T> viewModelClass,
    @Nullable final Bundle savedInstanceState) {
    final String id = fetchId(savedInstanceState);
    ViewModel viewModel = viewModels.get(id);

    if (viewModel == null) {
      try {
        viewModel = viewModelClass.newInstance();
      }
      catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      viewModels.put(id, viewModel);
      viewModel.onCreate(context, BundleUtils.maybeGetBundle(savedInstanceState, VIEW_MODEL_STATE_KEY));
    }

    return (T) viewModel;
  }

  public void destroy(@NonNull final ViewModel viewModel) {
    viewModel.onDestroy();

    Iterator<Map.Entry<String, ViewModel>> iterator = viewModels.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<String, ViewModel> entry = iterator.next();
      if (viewModel.equals(entry.getValue())) {
        iterator.remove();
      }
    }
  }

  public void save(@NonNull final ViewModel viewModel, @NonNull final Bundle envelope) {
    envelope.putString(VIEW_MODEL_ID_KEY, findIdForViewModel(viewModel));

    final Bundle state = new Bundle();
    viewModel.save(state);
    envelope.putBundle(VIEW_MODEL_STATE_KEY, state);
  }

  private String fetchId(@Nullable final Bundle savedInstanceState) {
    return savedInstanceState != null ?
      savedInstanceState.getString(VIEW_MODEL_ID_KEY) :
      UUID.randomUUID().toString();
  }

  private String findIdForViewModel(@NonNull final ViewModel viewModel) {
    for (final Map.Entry<String, ViewModel> entry : viewModels.entrySet()) {
      if (viewModel.equals(entry.getValue())) {
        return entry.getKey();
      }
    }

    throw new RuntimeException("Cannot find view model in map!");
  }
}
