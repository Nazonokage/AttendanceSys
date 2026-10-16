const message=document.querySelector('#message');
function show(text){message.textContent=text;}
async function request(url,options){const response=await fetch(url,options);let data;try{data=await response.json();}catch{throw Error('Request failed. Please try again.');}if(!response.ok)throw Error(data.message||'Request failed.');return data;}
async function scan(id){return request('/api/scan',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({idNum:id})});}
document.querySelectorAll('form[data-api]').forEach(form=>form.addEventListener('submit',async event=>{event.preventDefault();const button=form.querySelector('button');button.disabled=true;try{const values=new FormData(form);let options={method:'POST'};if(form.dataset.json){options.headers={'Content-Type':'application/json'};options.body=JSON.stringify(Object.fromEntries(values));}else options.body=values;const data=await request(form.dataset.api,options);show(data.message||`Imported ${data.imported}; duplicates ${data.duplicates}; invalid lines ${data.invalid}.`);if(form.dataset.refresh)setTimeout(()=>location.reload(),1500);}catch(error){show(error.message);}finally{button.disabled=false;}}));
if(document.querySelector('#reader')){
 let busy=false,lastId='',lastTime=0;
 if(typeof Html5QrcodeScanner==='undefined')show('Camera scanner could not load. Use student ID entry below.');
 else new Html5QrcodeScanner('reader',{fps:10,qrbox:250},false).render(async id=>{if(busy||(id===lastId&&Date.now()-lastTime<30000))return;busy=true;lastId=id;lastTime=Date.now();try{show((await scan(id)).message);}catch(error){show(error.message);}finally{busy=false;}},()=>{});
}

const qrDialog = document.querySelector('#qr-dialog');
if (qrDialog) {
 const closeQr = async () => {
  if (document.fullscreenElement === qrDialog) await document.exitFullscreen().catch(() => {});
  qrDialog.close();
 };
 document.querySelector('#qr-close').addEventListener('click', closeQr);
 qrDialog.addEventListener('cancel', event => { event.preventDefault(); closeQr(); });
 document.querySelectorAll('.open-qr').forEach(button => button.addEventListener('click', () => {
  document.querySelector('#qr-name').textContent = button.dataset.name;
  document.querySelector('#qr-student-id').textContent = 'Student ID: ' + button.dataset.studentId;
  document.querySelector('#qr-year').textContent = button.dataset.year ? 'Year ' + button.dataset.year : 'Year level unassigned';
  document.querySelector('#qr-error').textContent = '';
  const image = document.querySelector('#qr-image');
  image.onerror = () => {document.querySelector('#qr-error').textContent = 'QR code could not load. Close and try again.';};
  image.src = '/api/students/' + button.dataset.id + '/qr.png';
  const download = document.querySelector('#qr-download');
  download.href = image.src + '?download=true';
  qrDialog.showModal();
  if (qrDialog.requestFullscreen) qrDialog.requestFullscreen().catch(() => {});
 }));
}
const qrExport = document.querySelector('#qr-export');
if (qrExport) qrExport.addEventListener('submit', async event => {
 event.preventDefault(); const button = qrExport.querySelector('button'); button.disabled = true;
 try {
  const year = document.querySelector('#export-year').value;
  if (!year) throw Error('Choose a year level before downloading.');
  const response = await fetch('/api/students/qr-codes.pdf?yearLevel=' + encodeURIComponent(year));
  if (!response.ok) throw Error(response.status === 404 ? 'No students in this year level. Assign students to this year first.' : 'Unable to generate PDF. Please try again.');
  const url = URL.createObjectURL(await response.blob());
  const link = document.createElement('a'); link.href = url; link.download = 'year-' + year + '-student-qr-codes.pdf'; link.click();
  setTimeout(() => URL.revokeObjectURL(url), 60000); show('QR card PDF downloaded.');
 } catch (error) {show(error.message);} finally {button.disabled = false;}
});
