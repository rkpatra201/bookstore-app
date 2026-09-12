import { useEffect, useState } from "react"
import { CATALOG_URL, HEALTH_URL } from "../../constants/AppConstants";

export function Catalog(){

 const [books,setBooks] = useState([]);

 function loadData(){
    fetch(CATALOG_URL)
    .then(res=> res.json())
    .then(json=> {
        console.log(json);
        setBooks(json.data);
    })
 }   

 useEffect(()=>{
   loadData();
 });

 return <>
 <h1>Catalog</h1>
 {books.map(item=>{
    return <li key={item.id}>
        {JSON.stringify(item)}
    </li>
 })}
 </>
}