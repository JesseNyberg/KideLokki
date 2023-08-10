package com.skotfrii.kidelokki;

import com.skotfrii.kidelokki.JsonClasses.Root;

public interface FetchProductCallback {
    void onProductFetched (Root fetchedProduct);
}
