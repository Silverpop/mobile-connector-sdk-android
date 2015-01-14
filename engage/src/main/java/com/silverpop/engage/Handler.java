package com.silverpop.engage;

/**
 *
 * @param <S> success object type
 * @param <F> failure object type
 */
public interface Handler<S, F> {

    public void onSuccess(S success);

    public void onFailure(F failure);

}
