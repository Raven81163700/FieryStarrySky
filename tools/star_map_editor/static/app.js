const canvas = document.getElementById('canvas');
const ctx = canvas.getContext('2d');
const systemsList = document.getElementById('systems');
const linksList = document.getElementById('links');
const addBtn = document.getElementById('add-system');
const exportBtn = document.getElementById('export');
const linkModeBtn = document.getElementById('link-mode');
const gridInput = document.getElementById('grid-size');

let systems = [];
let links = [];
let nextSystemId = 1;
let dragging = null;
let linkMode = false;
let linkFrom = null;

function draw() {
  ctx.clearRect(0,0,canvas.width,canvas.height);
  drawGrid();
  // links
  ctx.strokeStyle = '#66f';
  ctx.lineWidth = 2;
  links.forEach(l => {
    const a = systems.find(s => s.id === l.from_id);
    const b = systems.find(s => s.id === l.to_id);
    if (!a || !b) return;
    ctx.beginPath();
    ctx.moveTo(a.x, a.y);
    ctx.lineTo(b.x, b.y);
    ctx.stroke();
  });
  // systems
  systems.forEach(s => {
    ctx.fillStyle = '#fff';
    ctx.beginPath();
    ctx.arc(s.x, s.y, 8, 0, Math.PI*2);
    ctx.fill();
    ctx.strokeStyle = '#000';
    ctx.stroke();
    ctx.fillStyle = '#000';
    ctx.font = '12px sans-serif';
    ctx.fillText(s.name, s.x+10, s.y+4);
  });
}

function drawGrid(){
  const g = parseInt(gridInput.value,10) || 20;
  ctx.strokeStyle = '#111';
  ctx.lineWidth = 0.3;
  for(let x=0;x<canvas.width;x+=g){
    ctx.beginPath(); ctx.moveTo(x,0); ctx.lineTo(x,canvas.height); ctx.stroke();
  }
  for(let y=0;y<canvas.height;y+=g){
    ctx.beginPath(); ctx.moveTo(0,y); ctx.lineTo(canvas.width,y); ctx.stroke();
  }
}

function addSystemAt(x,y){
  const name = prompt('System name','SYS'+nextSystemId) || ('SYS'+nextSystemId);
  const sec = parseInt(prompt('Security level (0-3)','2'),10) || 1;
  const s = {id: nextSystemId++, name: name, x: Math.round(x), y: Math.round(y), security: sec};
  systems.push(s);
  refreshLists();
  draw();
}

canvas.addEventListener('dblclick', (e)=>{
  const r = canvas.getBoundingClientRect();
  addSystemAt(e.clientX - r.left, e.clientY - r.top);
});

canvas.addEventListener('mousedown', (e)=>{
  const p = mousePos(e);
  const s = findSystemAt(p.x,p.y);
  if (s){ dragging = s; dragging.offsetX = p.x - s.x; dragging.offsetY = p.y - s.y; }
});

canvas.addEventListener('mousemove', (e)=>{
  if (!dragging) return;
  const p = mousePos(e);
  dragging.x = Math.round(p.x - dragging.offsetX);
  dragging.y = Math.round(p.y - dragging.offsetY);
  refreshLists();
  draw();
});

canvas.addEventListener('mouseup', (e)=>{
  if (dragging) { dragging = null; }
});

canvas.addEventListener('click', (e)=>{
  const p = mousePos(e);
  const s = findSystemAt(p.x,p.y);
  if (linkMode){
    if (s){
      if (!linkFrom){ linkFrom = s; highlightSystem(s.id); }
      else if (linkFrom.id !== s.id){
        links.push({from_id: linkFrom.id, to_id: s.id, link_type:1, cost:1});
        linkFrom = null; clearHighlights();
        refreshLists(); draw();
      }
    }
  }
});

function mousePos(e){
  const r = canvas.getBoundingClientRect();
  return {x: e.clientX - r.left, y: e.clientY - r.top};
}

function findSystemAt(x,y){
  return systems.find(s=> Math.hypot(s.x-x, s.y-y) < 12);
}

addBtn.addEventListener('click', ()=>{
  const x = canvas.width/2; const y = canvas.height/2; addSystemAt(x,y);
});

linkModeBtn.addEventListener('click', ()=>{
  linkMode = !linkMode; linkModeBtn.textContent = 'Link: ' + (linkMode? 'On':'Off');
  if (!linkMode){ linkFrom = null; clearHighlights(); }
});

exportBtn.addEventListener('click', ()=>{
  const payload = {systems: systems.map(s=>({id:s.id, name:s.name, x:s.x, y:s.y, security:s.security})), links: links, bodies: []};
  fetch('/export', {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(payload)})
    .then(r=> r.blob())
    .then(blob=>{
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = 'star_map_export.json';
      document.body.appendChild(a); a.click(); a.remove();
      URL.revokeObjectURL(url);
    });
});

function refreshLists(){
  systemsList.innerHTML = '';
  systems.forEach(s=>{
    const li = document.createElement('li');
    li.textContent = `${s.id}: ${s.name} @ ${s.x},${s.y} sec=${s.security}`;
    systemsList.appendChild(li);
  });
  linksList.innerHTML = '';
  links.forEach((l,i)=>{
    const li = document.createElement('li');
    li.textContent = `${l.from_id}->${l.to_id}`;
    linksList.appendChild(li);
  });
}

function highlightSystem(id){
  // simple visual: change canvas label color temporarily
  const s = systems.find(x=>x.id===id);
  if (!s) return;
  ctx.fillStyle = '#f00'; ctx.beginPath(); ctx.arc(s.x,s.y,10,0,Math.PI*2); ctx.fill();
}
function clearHighlights(){ draw(); }

// initial draw
refreshLists(); draw();
