import { useEffect } from "react"
import { CATALOG_URL, HEALTH_URL } from "../../constants/AppConstants";

export function Catalog(){

 function loadData(){
    fetch(HEALTH_URL)
    .then(res=> res.json())
    .then(data=> console.log(data))
 }   

 useEffect(()=>{
   loadData();
 });

 return <>
 <h1>Catalog</h1>
 </>
}