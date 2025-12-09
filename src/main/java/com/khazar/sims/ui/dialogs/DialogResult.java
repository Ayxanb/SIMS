package com.khazar.sims.ui.dialogs;

import java.util.HashMap;
import java.util.Map;

public class DialogResult {
  private final boolean confirmed;
  private final Map<String, Object> data;

  public DialogResult(boolean confirmed, Map<String, Object> data) {
    this.confirmed = confirmed;
    this.data = data;
  }

  public boolean isPresent() {
    return confirmed;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) data.get(key);
  }

  public Map<String, Object> asMap() {
    return new HashMap<>(data);
  }
}
