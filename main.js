// Change this to the root of your server
const API_ROOT = 'http://localhost:8080';

// Options for the disease checklist (NEEDED FOR NEW FEATURE)
const DISEASE_OPTIONS = ["HIV/AIDS", "Hepatitis B/C", "Severe Anemia", "None"];

// =======================================================
// AUTH UTILS
// =======================================================
function getAuthHeader() {
  const token = localStorage.getItem('jwtToken');
  return token ? { 'Authorization': `Bearer ${token}` } : {};
}

// =======================================================
// DONOR FUNCTIONS (READ/CRUD)
// =======================================================
async function getDonors(bloodGroup = "") {
  let url = API_ROOT + '/donors';
  if (bloodGroup && bloodGroup !== "All")
    url = API_ROOT + '/donors/search?bloodGroup=' + bloodGroup;

  const headers = { 'Content-Type': 'application/json', ...getAuthHeader() };
  const res = await fetch(url, { headers });
  if (res.status === 401 || res.status === 403) {
    localStorage.removeItem('jwtToken');
    alert("Session expired. Please log in again.");
    window.location.href = 'index.html';
    return [];
  }
  return res.ok ? res.json() : [];
}

async function deleteDonor(id) {
  if (!confirm('Are you sure you want to delete this donor record?')) return;

  const res = await fetch(`${API_ROOT}/donors/${id}`, {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json', ...getAuthHeader() }
  });

  if (res.ok) fillDonorsIntoUI();
  else if (res.status === 401 || res.status === 403) {
    alert("Authentication required to delete.");
    window.location.href = 'index.html';
  } else {
    alert('Error deleting donor.');
  }
}

// FIX: Updated to handle new 'diseases' field
async function updateDonor(id, data) {
  // Collect checked diseases from the modal form
  const diseases = [];
  document.querySelectorAll('#edit-diseases-container input[name="diseases"]:checked').forEach(checkbox => {
      diseases.push(checkbox.value);
  });
  data.diseases = diseases.join(', '); // Send as comma-separated string

  const res = await fetch(`${API_ROOT}/donors/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
    body: JSON.stringify(data)
  });

  if (res.ok) {
    const modal = bootstrap.Modal.getInstance(document.getElementById('editDonorModal'));
    if (modal) modal.hide();
    fillDonorsIntoUI();
  } else {
    alert('Error updating donor.');
  }
}

async function fillDonorsIntoUI() {
  const donors = await getDonors();
  const container = document.querySelector('#requestsContainer');
  const donorCountElement = document.querySelector('#donorCountBadge');
  if (!container || !donorCountElement) return;

  container.innerHTML = '';
  donorCountElement.textContent = `${donors.length} Total`;

  if (donors.length === 0) {
    container.innerHTML =
      '<div class="alert alert-info mt-3">No donors found.</div>';
    return;
  }

  donors.forEach(donor => {
    const el = document.createElement('div');
    el.className = 'donor-list-item d-flex justify-content-between align-items-center';

    // NEW: Format diseases for display
    const diseaseList = donor.diseases && donor.diseases.trim().length > 0
        ? `<span class="badge bg-warning text-dark me-2">${donor.diseases.split(',').map(s => s.trim()).join('</span><span class="badge bg-warning text-dark me-2">')}</span>`
        : 'None';
        
    el.innerHTML = `
      <div>
        <span class="badge bg-danger">${donor.bloodGroup}</span>
        <strong class="text-dark">${donor.name}</strong>
        <div class="small text-muted">${donor.city} - ${donor.contact} (${donor.email})</div>
        <div class="small mt-1">Conditions: ${diseaseList}</div> <!-- Display diseases -->
      </div>
      <div class="d-flex gap-2">
        <button class="btn btn-outline-secondary btn-sm btn-edit" 
          data-bs-toggle="modal" 
          data-bs-target="#editDonorModal"
          data-id="${donor.id}" 
          data-name="${donor.name}" 
          data-email="${donor.email}"
          data-bloodgroup="${donor.bloodGroup}"
          data-city="${donor.city}" 
          data-contact="${donor.contact}"
          data-diseases="${donor.diseases || ''}"> <!-- Pass diseases for edit modal -->
          <i class="fas fa-edit"></i> Edit
        </button>
        <button class="btn btn-danger btn-sm btn-delete" data-id="${donor.id}">
          <i class="fas fa-trash-alt"></i> Delete
        </button>
      </div>`;
    container.appendChild(el);
  });

  document.querySelectorAll('.btn-delete').forEach(b =>
    b.addEventListener('click', () => deleteDonor(b.dataset.id))
  );
  document.querySelectorAll('.btn-edit').forEach(b =>
    b.addEventListener('click', () => openEditModal(b.dataset))
  );
}

// FIX: Updated to handle new 'diseases' field when opening modal
function openEditModal(d) {
  document.getElementById('edit-id').value = d.id;
  document.getElementById('edit-name').value = d.name;
  document.getElementById('edit-email').value = d.email;
  document.getElementById('edit-bloodGroup').value = d.bloodgroup;
  document.getElementById('edit-city').value = d.city;
  document.getElementById('edit-contact').value = d.contact;

  // NEW LOGIC: Inject and check disease checkboxes
  const container = document.getElementById('edit-diseases-container');
  // Convert stored string back to an array for checking
  const existingDiseases = (d.diseases || '').split(',').map(s => s.trim()).filter(s => s); 
  container.innerHTML = ''; // Clear container

  DISEASE_OPTIONS.forEach(disease => {
      const isChecked = existingDiseases.includes(disease);
      const uniqueId = `edit-check-${disease.replace(/[^a-zA-Z0-9]/g, '')}`;
      container.innerHTML += `
          <div class="form-check">
              <input class="form-check-input" type="checkbox" value="${disease}" id="${uniqueId}" name="diseases" ${isChecked ? 'checked' : ''}>
              <label class="form-check-label" for="${uniqueId}">${disease}</label>
          </div>
      `;
  });
}

// =======================================================
// BLOOD REQUEST FUNCTIONS (NO CHANGES)
// =======================================================
async function getBloodRequests() {
  const res = await fetch(API_ROOT + '/requests', {
    headers: { 'Content-Type': 'application/json', ...getAuthHeader() }
  });
  if (!res.ok) return [];
  return res.json();
}

async function deleteBloodRequest(id) {
  if (!confirm('Are you sure you want to delete this blood request?')) return;
  const res = await fetch(`${API_ROOT}/requests/${id}`, {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json', ...getAuthHeader() }
  });

  if (res.ok) {
    alert('Blood request marked as handled.');
    fillBloodRequestsIntoUI(); // refresh list
  } else if (res.status === 401 || res.status === 403) {
    alert("Authentication required or expired.");
    window.location.href = 'index.html';
  } else {
    alert('Error deleting blood request.');
  }
}

async function fillBloodRequestsIntoUI() {
  const requests = await getBloodRequests();
  const container = document.querySelector('#bloodRequestsContainer');
  const countElement = document.querySelector('#pendingRequestCount');
  if (!container || !countElement) return;

  countElement.textContent = requests.length;
  container.innerHTML = '';

  if (requests.length === 0) {
    container.innerHTML = `<div class="alert alert-success small m-0 p-2">No urgent requests currently.</div>`;
    return;
  }

  requests.forEach(req => {
    const el = document.createElement('div');
    el.className = 'request-item border rounded p-2 mb-2 bg-white';
    el.innerHTML = `
      <div class="d-flex justify-content-between align-items-start">
        <div>
          <span class="badge bg-danger mb-1">${req.bloodGroup}</span>
          <strong class="d-block">${req.hospitalName}</strong>
          <div class="small-muted">City: ${req.city}</div>
          <div class="small-muted">Contact: ${req.contactPhone}</div>
        </div>
        <div class="small-muted text-end">
          Req. By: ${new Date(req.requiredBy).toLocaleDateString()}
        </div>
      </div>
      <div class="mt-2 pt-2 border-top d-flex gap-2">
        <a href="tel:${req.contactPhone}" class="btn btn-outline-danger btn-sm flex-fill">Call Contact</a>
        <button class="btn btn-danger btn-sm flex-fill btn-delete-request" data-id="${req.id}">Delete</button>
      </div>`;
    container.appendChild(el);
  });

  // attach delete listeners
  document.querySelectorAll('.btn-delete-request').forEach(btn =>
    btn.addEventListener('click', () => deleteBloodRequest(btn.dataset.id))
  );
}

// =======================================================
// PUBLIC FORM HANDLERS (Minimal Change)
// =======================================================
document.getElementById('requestForm')?.addEventListener('submit', async e => {
  e.preventDefault();
  
  const formData = new FormData(e.target);
  const data = Object.fromEntries(formData.entries());

  // NEW LOGIC: Manually collect all checked diseases
  const diseases = [];
  formData.getAll('diseases').forEach(disease => {
      if (disease) diseases.push(disease);
  });
  data.diseases = diseases.join(', ');

  const res = await fetch(API_ROOT + '/donors', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  if (res.ok) window.location = 'donor-registered.html';
  else alert('Error submitting registration.');
});

document.getElementById('bloodRequestForm')?.addEventListener('submit', async e => {
  e.preventDefault();
  const data = Object.fromEntries(new FormData(e.target).entries());
  const res = await fetch(API_ROOT + '/requests', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });
  if (res.ok) window.location = 'request-submitted.html';
  else alert('Error submitting request.');
});

// =======================================================
// ADMIN LOGIN FORM (WORKING LOGIC)
// =======================================================
document.addEventListener('submit', async e => {
  if (e.target.id === 'loginFormPopover') {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.target).entries());
    const res = await fetch(API_ROOT + '/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: data.email, password: data.password })
    });

    const popoverEl = document.getElementById('adminLoginButton');
    const popover = bootstrap.Popover.getInstance(popoverEl);
    if (popover) popover.hide();

    if (res.ok) {
      const json = await res.json();
      localStorage.setItem('jwtToken', json.token);
      window.location.href = 'dashboard.html';
    } else alert('Login failed. Invalid username or password.');
  }
});

// =======================================================
// SEARCH & EDIT FORM HANDLERS (Updated to collect diseases)
// =======================================================
document.getElementById('editForm')?.addEventListener('submit', async e => {
  e.preventDefault();
  
  const formData = new FormData(e.target);
  const data = Object.fromEntries(formData.entries());

  // NEW LOGIC: Manually collect all diseases from checkboxes
  const diseases = [];
  formData.getAll('diseases').forEach(disease => {
      if (disease) diseases.push(disease);
  });
  data.diseases = diseases.join(', ');

  updateDonor(data.id, data);
});

document.getElementById('searchForm')?.addEventListener('submit', async e => {
  e.preventDefault();
  const bloodGroup = document.getElementById('searchBloodGroup').value;
  const donors = await getDonors(bloodGroup);
  const container = document.querySelector('#requestsContainer');
  container.innerHTML = donors.length
    ? ''
    : '<div class="alert alert-warning mt-3">No donors found.</div>';
  fillDonorsIntoUI();
});

// =======================================================
// LOGOUT AND INIT
// =======================================================
// Logout Functionality - Now performs hard clear and redirect
document.getElementById('logoutButton')?.addEventListener('click', () => {
  localStorage.removeItem('jwtToken');
  window.location.reload(true); 
  window.location.href = 'index.html'; 
});


document.addEventListener('DOMContentLoaded', () => {
  if (document.querySelector('#requestsContainer')) {
    fillDonorsIntoUI();
    fillBloodRequestsIntoUI();
  }
  
  // FIX: Initialize ALL popovers on the page once.
  // This relies on the HTML data-bs-content="#ID" being correct.
   // Initialize Bootstrap popover for admin login

  const popoverTriggerEl = document.getElementById('adminLoginButton');

  const popoverContentEl = document.getElementById('loginPopoverContentContainer');

   if (popoverTriggerEl && popoverContentEl) {
    new bootstrap.Popover(popoverTriggerEl, {
      html: true,
      sanitize: false,
      title: 'Login',
      content: popoverContentEl.innerHTML
    });
  }
}); // 👈 closes the document.addEventListener('DOMContentLoaded', ...)
