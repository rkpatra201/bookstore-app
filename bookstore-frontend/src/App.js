import React from 'react';
import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';

import { Catalog } from './components/catalog/CatalogComponent';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { CatalogItemDetails } from './components/catalog/SingleCatalogComponent';
import TopNavbar from './components/navigation/TopNavbar';
import { Cart } from './components/cart/CartComponent';
import { NotFound } from './components/navigation/NotFound';
import { OrderHistory } from './components/orders/OrderComponent';
import { CartProvider } from './providers/CartProvider';
import { MeComponent } from './components/me/MeComponent';

function App() {
  return (
    <BrowserRouter>
      {/* Wrap everything in the global state bucket */}
      <CartProvider>
        <div className="App">
          <TopNavbar />
          <Routes>
            <Route path='/' element={<Catalog/>}/>
            <Route path='/book/:id' element={<CatalogItemDetails/>}/>
            <Route path='/cart' element={<Cart/>}/>
            <Route path='/orders' element={<OrderHistory/>}/>
            <Route path='/me' element={<MeComponent/>}/>
            <Route path='/*' element={<NotFound/>}/>
          </Routes>
        </div>
      </CartProvider>
    </BrowserRouter>
  );
}

export default App;
