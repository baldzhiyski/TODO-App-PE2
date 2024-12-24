<template>
  <div class="container mt-5">
    <!-- Header -->
    <div class="text-center mb-4">
      <h1 class="display-4">
        <i class="fas fa-tasks text-primary"></i> ToDos Management
      </h1>
      <p class="lead text-muted">Manage your tasks efficiently with enhanced features and styling.</p>
    </div>

    <!-- Download Button -->
    <div class="text-center mb-4">
      <button
          @click="downloadCSV"
          class="btn btn-primary btn-lg"
          :disabled="isDownloading"
      >
        <i class="fas fa-download me-2"></i> Download CSV
        <span v-if="isDownloading" class="spinner-border spinner-border-sm ms-2" role="status" aria-hidden="true"></span>
      </button>
    </div>

    <!-- Filters and Sorting -->
    <div class="row g-3 mb-4 align-items-end">
      <div class="col-md-4">
        <label for="filterTitle" class="form-label">Filter by Title</label>
        <input
            v-model="filters.title"
            type="text"
            class="form-control"
            id="filterTitle"
            placeholder="Search tasks..."
        />
      </div>
      <div class="col-md-4">
        <label for="sortBy" class="form-label">Sort By</label>
        <select v-model="filters.sortBy" id="sortBy" class="form-select">
          <option value="title">Title</option>
          <option value="dueDate">Due Date</option>
        </select>
      </div>
      <div class="col-md-4">
        <label for="sortOrder" class="form-label">Sort Order</label>
        <select v-model="filters.sortOrder" id="sortOrder" class="form-select">
          <option value="asc">Ascending</option>
          <option value="desc">Descending</option>
        </select>
      </div>
    </div>

    <!-- ToDo Lists -->
    <div class="row g-5">
      <!-- Pending ToDos -->
      <div class="col-lg-6">
        <div class="card border-warning shadow">
          <div class="card-header bg-warning text-white">
            <h3>
              <i class="fas fa-hourglass-half me-2"></i> Pending ToDos
            </h3>
          </div>
          <ul class="list-group list-group-flush">
            <li
                v-for="todo in filteredAndSortedToDos(false)"
                :key="todo.id"
                class="list-group-item d-flex justify-content-between align-items-start"
            >
              <div class="flex-grow-1">
                <h5 class="mb-1">
                  <i class="fas fa-clipboard-list text-primary me-2"></i> {{ todo.title }}
                </h5>
                <p class="text-muted mb-1 small">
                  <i class="fas fa-calendar-plus me-1"></i> Created: {{ formatDate(todo.createdDate) }}<br />
                  <i class="fas fa-calendar-alt me-1"></i> Due: {{ formatDate(todo.dueDate) }}<br />
                  <i class="fas fa-tags me-1"></i> Category: {{ todo.category }}
                </p>
                <p class="mb-0 small">
                  <i class="fas fa-users me-1"></i> Assignees:
                  <span v-if="todo.assigneeList && todo.assigneeList.length">
                    {{ todo.assigneeList.map(a => a.prename + " " + a.name).join(", ") }}
                  </span>
                  <span v-else>None</span>
                </p>
              </div>
              <div class="btn-group" role="group">
                <!-- Mark as Finished -->
                <button class="btn btn-outline-success btn-sm" @click="markAsFinished(todo.id)" title="Mark as Finished">
                  <i class="fas fa-check"></i>
                </button>

                <!-- Delete Task -->
                <button class="btn btn-outline-danger btn-sm" @click="deleteToDo(todo.id)" title="Delete Task">
                  <i class="fas fa-trash-alt"></i>
                </button>

                <!-- Assign User -->
                <button class="btn btn-outline-info btn-sm" @click="openAssignModal(todo)" title="Assign User">
                  <i class="fas fa-user-plus"></i>
                </button>

                <!-- Edit Task -->
                <button class="btn btn-outline-dark btn-sm" @click="openEditModal(todo)" title="Edit Task">
                  <i class="fas fa-edit"></i>
                </button>
              </div>

            </li>
            <li v-if="filteredAndSortedToDos(false).length === 0" class="list-group-item text-center text-muted">
              <i class="fas fa-info-circle"></i> No pending ToDos.
            </li>
          </ul>
        </div>
      </div>

      <!-- Finished ToDos -->
      <div class="col-lg-6">
        <div class="card border-success shadow">
          <div class="card-header bg-success text-white d-flex align-items-center justify-content-between">
            <h3>
              <i class="fas fa-check-circle me-2"></i> Finished ToDos
            </h3>
            <button class="btn btn-sm btn-light" @click="toggleFinishedVisibility">
              <i class="fas" :class="showFinished ? 'fa-eye-slash' : 'fa-eye'"></i>
            </button>
          </div>
          <ul v-if="showFinished" class="list-group list-group-flush">
            <li
                v-for="todo in filteredAndSortedToDos(true)"
                :key="todo.id"
                class="list-group-item d-flex justify-content-between align-items-start"
            >
              <div class="flex-grow-1">
                <h5 class="mb-1">
                  <i class="fas fa-check text-success me-2"></i> {{ todo.title }}
                </h5>
                <p class="text-muted mb-1 small">
                  <i class="fas fa-calendar-check me-1"></i> Completed: {{ formatDate(todo.finishedDate) }}<br />
                  <i class="fas fa-calendar-alt me-1"></i> Due: {{ formatDate(todo.dueDate) }}<br />
                  <i class="fas fa-tags me-1"></i> Category: {{ todo.category }}
                </p>
                <p class="mb-0 small">
                  <i class="fas fa-users me-1"></i> Assignees:
                  <span v-if="todo.assigneeResponseList && todo.assigneeResponseList.length">
                    {{ todo.assigneeResponseList.map(a => a.prename + " " + a.name).join(", ") }}
                  </span>
                  <span v-else>None</span>
                </p>
              </div>
              <button class="btn btn-warning btn-sm" @click="revertToDoStatus(todo.id)">
                <i class="fas fa-undo"></i>
              </button>
            </li>
            <li v-if="filteredAndSortedToDos(true).length === 0" class="list-group-item text-center text-muted">
              <i class="fas fa-info-circle"></i> No finished ToDos.
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Create ToDo Button -->
    <div class="text-center mt-5">
      <button class="btn btn-primary btn-lg" @click="openCreateModal">
        <i class="fas fa-plus"></i> Create New ToDo
      </button>
    </div>

    <!-- Modals -->
    <!-- Edit ToDo Modal -->
    <ModalComponent v-model:isVisible="isEditModalVisible">
      <h3><i class="fas fa-user-plus"></i> Update ToDo Full Info</h3>
      <form @submit.prevent="saveEdits">
        <div class="mb-3">
          <label for="title" class="form-label">Title</label>
          <input type="text" v-model="editToDo.title" class="form-control" id="title"  />
        </div>
        <div class="mb-3">
          <label for="description" class="form-label">Description</label>
          <input type="text" v-model="editToDo.description" class="form-control" id="description"  />
        </div>
        <div class="mb-3">
          <label for="dueDate" class="form-label">Due Date</label>
          <div class="input-group">
            <input type="date" v-model="editToDo.dueDate" class="form-control" id="dueDate"  placeholder="Select a date"/>
            <span class="input-group-text"><i class="fas fa-calendar-day"></i></span>
            <!-- Font Awesome calendar icon -->
          </div>
        </div>
        <div class="mb-3">
          <label for="finished" class="form-label">Is Finished?</label>
          <div class="form-check">
            <input
                type="checkbox"
                v-model="editToDo.finished"
                class="form-check-input"
                id="finished"
            />
            <label class="form-check-label" for="finished">
              Mark as Finished
            </label>
          </div>
        </div>
        <div class="mb-3">
          <label for="assigneeIdInput" class="form-label">Update Assignees List</label>
          <div class="input-group">
            <input
                type="number"
                v-model="editDtoId"
                class="form-control"
                id="assigneeIdInput"
                placeholder="Enter Assignee ID"
            />
            <button type="button" class="btn btn-outline-secondary" @click="addAssigneeIdUpdate">
              Add
            </button>
          </div>

        </div>
        <div class="mt-3">
          <h5>Current Assignees:</h5>
          <ul>
            <li v-for="(id, index) in editToDo.assigneeIdList" :key="index">{{ id }}</li>
          </ul>
        </div>
        <div class="d-flex justify-content-end mt-3">
          <button type="submit" class="btn btn-primary">
            <i class="fas fa-save"></i> Save
          </button>
        </div>
      </form>
    </ModalComponent>

    <!-- Create ToDo Modal -->
    <ModalComponent v-model:isVisible="isCreateModalVisible">
      <h3><i class="fas fa-user-plus"></i> Create New ToDo</h3>
      <form @submit.prevent="createToDo">
        <div class="mb-3">
          <label for="title" class="form-label">Title</label>
          <input type="text" v-model="newToDo.title" class="form-control" id="title" required/>
        </div>
        <div class="mb-3">
          <label for="description" class="form-label">Description</label>
          <input type="text" v-model="newToDo.description" class="form-control" id="description" required/>
        </div>
        <div class="mb-3">
          <label for="dueDate" class="form-label">Due Date</label>
          <div class="input-group">
            <input type="date" v-model="newToDo.dueDate" class="form-control" id="dueDate"/>
            <span class="input-group-text"><i class="fas fa-calendar-day"></i></span>
            <!-- Font Awesome calendar icon -->
          </div>
        </div>
        <div class="mb-3">
          <label for="assigneeIdList" class="form-label">Assignees Ids</label>
          <div class="input-group">
            <input type="number" v-model="newAssigneeId" class="form-control" id="assigneeId"
                   placeholder="Enter Assignee ID"/>
            <button type="button" class="btn btn-outline-secondary" @click="addAssigneeId">Add</button>
          </div>
          <div class="mt-2">
            <ul>
              <li v-for="(assigneeId, index) in newToDo.assigneeIdList" :key="index">{{ assigneeId }}</li>
            </ul>
          </div>
        </div>
        <div class="d-flex justify-content-end">
          <button type="submit" class="btn btn-primary">
            <i class="fas fa-save"></i> Save
          </button>
        </div>
      </form>
    </ModalComponent>


    <!-- Create Assign  Modal -->
    <!-- Create Assign Modal -->
    <ModalComponent v-model:isVisible="isAssignModalVisible">
      <h3><i class="fas fa-user-plus"></i> Assign to the Task</h3>
      <form @submit.prevent="saveEdits">
        <div class="mb-3">
          <label for="assigneeSelect" class="form-label">Add Assignees</label>
          <select
              id="assigneeSelect"
              v-model="selectedAssignee"
              class="form-select"
          >
            <!-- Check if assignees is empty -->
            <option v-if="assignees.length === 0" disabled>
              No assignees available
            </option>
            <!-- Loop through assignees -->
            <option v-for="ass in assignees" :key="ass.id" :value="ass">
              {{ ass.prename + " " + ass.name }}
            </option>
          </select>
          <button
              type="button"
              class="btn btn-outline-secondary mt-2"
              @click="addAssignee"
              :disabled="!selectedAssignee"
          >
            Add
          </button>
        </div>
        <div class="mt-3">
          <h5>Current Assignees:</h5>
          <ul>
            <li
                v-for="(id, index) in editToDo.assigneeIdList"
                :key="index"
            >
              {{ getUserNameById(id) }}
              <button
                  type="button"
                  class="btn btn-sm btn-outline-danger ms-2 p-1"
                  @click="removeAssignee(index)"
                  title="Remove Assignee"
              >
                <i class="fas fa-trash-alt"></i>
              </button>
            </li>
          </ul>
        </div>
        <div class="d-flex justify-content-end mt-3">
          <button type="submit" class="btn btn-primary">
            <i class="fas fa-save"></i> Save
          </button>
        </div>
      </form>
    </ModalComponent>


  </div>
</template>

<script>
import apiClient from "../lib/apiClient";
import ModalComponent from "../components/ModalComponent.vue";
import {createToast} from "../ts/toasts.ts";
import {faCheck, faInfo, faXmark} from "@fortawesome/free-solid-svg-icons";

export default {
  components: {ModalComponent},
  data() {
    return {
      toDos: [],
      filters: {
        title: "",
        sortBy: "title",
        sortOrder: "asc",
      },
      showFinished: true,
      selectedToDo: {},
      selectedAssignee: null,
      assignees: [],
      isCreateModalVisible: false,
      isEditModalVisible: false,
      isAssignModalVisible:false,
      isDownloading: false,
      newToDo: {
        title: "",
        description: "",
        dueDate: "",
        finished: false,
        assigneeIdList: []
      },
      editToDo: {
        title: "",
        dueDate: "",
        description: "",
        finished : false,
        assigneeIdList: []
      }
    };
  },
  created() {
    this.fetchToDos();
    this.fetchAssignees();
  },
  methods: {
    async fetchToDos() {
      try {

        const response = await apiClient.get('todos');
        this.toDos = response;
      } catch (error) {
        console.error('Error fetching ToDos:', error);
      }
    },
    async fetchAssignees() {
      try {
        const response = await apiClient.get('assignees');
        this.assignees = response;
      } catch (error) {
        console.error('Error fetching assignees:', error);
      }
    },
    formatDate(date) {
      return new Date(date).toLocaleDateString();
    },
    filteredAndSortedToDos(isFinished) {
      return this.toDos
          .filter(todo =>
              todo.title.toLowerCase().includes(this.filters.title.toLowerCase())
          )
          .filter(todo => todo.finished === isFinished)
          .sort((a, b) => {
            if (this.filters.sortBy === "title") {
              return this.filters.sortOrder === "asc"
                  ? a.title.localeCompare(b.title)
                  : b.title.localeCompare(a.title);
            } else if (this.filters.sortBy === "dueDate") {
              return this.filters.sortOrder === "asc"
                  ? new Date(a.dueDate) - new Date(b.dueDate)
                  : new Date(b.dueDate) - new Date(a.dueDate);
            }
          });
    },
    async markAsFinished(id) {
      try {
        const currentTimestamp = Date.now();
        await apiClient.put(`todos/${id}`, {
          title: "",
          dueDate: "",
          description: "",
          finished : true,
          assigneeIdList: [],
          finishedDate: currentTimestamp // Add the finishedDate as a timestamp
        });
        this.fetchToDos(); // Refresh the list
      } catch (error) {
        console.error('Error marking ToDo as finished:', error);
      }
    },
    async revertToDoStatus(id) {
      try {
        await apiClient.put(`todos/${id}`, {
          title: "",
          dueDate: "",
          description: "",
          finished : false,
          assigneeIdList: [],
        });
        this.fetchToDos(); // Refresh the list
      } catch (error) {
        console.error('Error reverting ToDo status:', error);
      }
    },
    async createToDo() {
      try {
        if (this.newToDo.dueDate) {
          this.newToDo.dueDate = new Date(this.newToDo.dueDate).getTime();
        }
        await apiClient.post('todos', this.newToDo);
        this.fetchToDos(); // Refresh the list
        this.newToDo.title = '';
        this.newToDo.dueDate = '';
        this.isCreateModalVisible = false;
        createToast({
          title: "Successfully uploaded Todo",  // Toast title
          message: "You have uploaded todo !",  // The concatenated error messages,
          icon: faInfo,
          type: "success",  // Toast type (error)
          timeout: 5,  // Duration for the toast in seconds
        });

      } catch (error) {
        this.isCreateModalVisible = false;
        if (error.fieldErrors && Object.keys(error.fieldErrors).length > 0) {
          this.showValidationErrors(error?.fieldErrors);
        }
        console.log(error.message);
        createToast({
          title: "Invalid Operation !",  // Toast title
          message: error.message,  // The concatenated error messages,
          icon: faXmark,
          type: "error",  // Toast type (error)
          timeout: 5,  // Duration for the toast in seconds
        });
      }
    },
    openCreateModal() {
      this.newToDo = {
        title: "",
        description: "",
        dueDate: "",
        assigneeIdList: []
      }
      this.isCreateModalVisible = true;
    },

    showValidationErrors(errors) {

      console.log(errors)
      // Convert the error object into an array of error messages
      const errorMessages = Object.entries(errors)  // Convert object to an array of [key, value] pairs
          .map(([field, message]) => `${message}`)  // Format each error message
          .join('<br/>');  // Join the messages with <br/> to create line breaks

      // Create the toast with the combined error messages
      createToast({
        title: "Validation Errors",  // Toast title
        message: errorMessages,  // The concatenated error messages,
        icon: faXmark,
        type: "error",  // Toast type (error)
        timeout: 5,  // Duration for the toast in seconds
      });
    },
    addAssigneeId() {
      // Check if the entered value is a valid integer
      if (Number.isInteger(Number(this.newAssigneeId)) && this.newAssigneeId !== '') {
        // If valid, add to assigneeIdList
        this.newToDo.assigneeIdList.push(Number(this.newAssigneeId));
        console.log(this.newToDo.assigneeIdList)
        createToast({
          title: "Added id of assignee",  // Toast title
          message: 'Successfully pushed id',  // The concatenated error messages,
          icon: faInfo,
          type: "success",  // Toast type (error)
          timeout: 5,  // Duration for the toast in seconds
        });
        this.newAssigneeId = ''; // Reset the input field after adding
      } else {
        // If not valid, show an error message (or handle accordingly)
        createToast({
          title: "Validation Errors",  // Toast title
          message: 'Please enter a valid integer for the Assignee ID.',  // The concatenated error messages,
          icon: faXmark,
          type: "error",  // Toast type (error)
          timeout: 5,  // Duration for the toast in seconds
        });
      }
    },
    addAssigneeIdUpdate(){
      if (Number.isInteger(Number(this.editDtoId)) && this.editDtoId !== '') {
        // If valid, add to assigneeIdList
        this.editToDo.assigneeIdList.push(Number(this.editDtoId));
        console.log(this.editToDo.assigneeIdList)
        createToast({
          title: "Added id of assignee",  // Toast title
          message: 'Successfully pushed id',  // The concatenated error messages,
          icon: faInfo,
          type: "success",  // Toast type (error)
          timeout: 5,  // Duration for the toast in seconds
        });
        this.editDtoId = ''; // Reset the input field after adding
      } else {
        // If not valid, show an error message (or handle accordingly)
        createToast({
          title: "Validation Errors",  // Toast title
          message: 'Please enter a valid integer for the Assignee ID.',  // The concatenated error messages,
          icon: faXmark,
          type: "error",  // Toast type (error)
          timeout: 5,  // Duration for the toast in seconds
        });
      }
    },

    openEditModal(todo) {
      this.editToDo.id=todo.id;
      this.isEditModalVisible = true;

    },
    openAssignModal(todo){
      this.editToDo.id = todo.id
      this.isAssignModalVisible=true;
    },
    async downloadCSV() {
      this.isDownloading = true; // Start download process

      try {
        // Use the get method from apiClient to fetch the CSV file
        const response = await apiClient.getCsv('csv-downloads/todos', {
          responseType: 'blob', // This ensures the response is treated as a binary Blob (CSV)
          headers: {
            'Content-Type': 'application/csv',
          },
        });

        // Assuming response is an object containing a 'todos' array or a similar structure
        // Ensure to handle the todos array properly

        // Check if the todos array is empty
        if (this.toDos.length === 0) {
          createToast({
            title: "Download failed",  // Toast title
            message: "No ToDos found. Download cannot be completed.",  // Error message
            icon: faXmark,
            type: "error",  // Toast type (error)
            timeout: 5,  // Duration for the toast in milliseconds (5 seconds)
          });
          this.isDownloading = false; // Reset the download flag
          return; // Exit the function as there's no need to proceed with the download
        }

        // Convert the response to a Blob and create a download link
        const url = window.URL.createObjectURL(response);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'todos.csv'); // You can name the file as per your requirement
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        console.error(error);

        // Show a toast message if there was an error during download
        createToast({
          title: "Download failed",  // Toast title
          message: "There was an error while downloading the file.",  // Error message
          icon: faXmark,
          type: "error",  // Toast type (error)
          timeout: 5,  // Duration for the toast in milliseconds (5 seconds)
        });
      } finally {
        this.isDownloading = false; // End download process
      }
    },
    async saveEdits() {
      try {
        console.log(this.editToDo)
        if (this.editToDo.dueDate) {
          this.editToDo.dueDate = new Date(this.editToDo.dueDate).getTime();
        }
        await apiClient.put(`todos/${this.editToDo.id}`, this.editToDo);
        const successMessage = "You have successfully edited the chosen todo !"
        this.editToDo={
          title: "",
              dueDate: "",
              description: "",
              assigneeIdList: [],
          finished: false
        };
        createToast({
          title: "Successfully  edited  todo !",  // Toast title
          message: successMessage,  // The concatenated error messages,
          icon: faInfo,
          type: "success",  // Toast type (error)
          timeout: 5,  // Duration for the toast in seconds
        });
        this.fetchToDos(); // Refresh the list
        this.isAssignModalVisible=false;
        this.isEditModalVisible = false;
      } catch (error) {
        this.isAssignModalVisible=false;
        this.isEditModalVisible = false;
        createToast({
          title: "Invalid Operation !",  // Toast title
          message: error.message,  // The concatenated error messages,
          icon: faXmark,
          type: "error",  // Toast type (error)
          timeout: 5,  // Duration for the toast in seconds
        });
      }
    },
    async deleteToDo(id) {
      try {
        await apiClient.delete(`todos/${id}`);
        createToast({
          title: "Successfully operation !",  // Toast title
          message: "Successfully deleted todo with id  " + id + "!",  // The concatenated error messages,
          icon: faInfo,
          type: "success",  // Toast type (error)
          timeout: 5,  // Duration for the toast in seconds
        });
        this.fetchToDos(); // Refresh the list
      } catch (error) {
        createToast({
          title: "Invalid Operation !",  // Toast title
          message: error.message,  // The concatenated error messages,
          icon: faXmark,
          type: "error",  // Toast type (error)
          timeout: 5,  // Duration for the toast in seconds
        });
        console.error('Error deleting ToDo:', error);
      }
    },
    toggleFinishedVisibility() {
      this.showFinished = !this.showFinished;
    },
    addAssignee() {
      if (
          this.selectedAssignee &&
          !this.editToDo.assigneeIdList.includes(this.selectedAssignee.id)
      ) {
        this.editToDo.assigneeIdList.push(this.selectedAssignee.id); // Push ID only
        this.selectedAssignee = null; // Reset selection
      }
    },
    removeAssignee(index) {
      this.editToDo.assigneeIdList.splice(index, 1);
    },
    getUserNameById(id) {
      const assignee = this.assignees.find((value) => value.id === id);
      return assignee ?assignee.prename + " " +  assignee.name  : "Unknown User";
    },
  }
};
</script>

<style scoped>
/* Customize button styling */
.btn-primary {
  background-color: #007bff;
  border-color: #007bff;
  padding: 10px 20px;
  font-size: 16px;
}

.btn-primary:hover {
  background-color: #0056b3;
  border-color: #004085;
}

.spinner-border-sm {
  border-width: 2px;
}

/* Add some spacing and rounded corners */
button {
  border-radius: 50px;
}
</style>