import logo from './logo.svg';
import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css';

import { Catalog } from './components/catalog/CatalogComponent';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { CatalogItemDetails } from './components/catalog/SingleCatalogComponent';
import TopNavbar from './components/navigation/TopNavbar';
import { Cart } from './components/cart/CartComponent';
import { NotFound } from './components/navigation/NotFound';
import { OrderHistory } from './components/orders/OrderComponent';

function App() {

  return (
    <BrowserRouter>
    <div className="App">
    <TopNavbar/>
      <Routes>
        <Route path='/' element={<Catalog/>}/>
        <Route path='/book/:id' element={<CatalogItemDetails/>}/>
        <Route path='/cart' element={<Cart/>}/>
        <Route path='/orders' element={<OrderHistory/>}/>
        <Route path='/*' element={<NotFound/>}/>
      </Routes>
    </div>
    </BrowserRouter>
  );
}

export default App;
