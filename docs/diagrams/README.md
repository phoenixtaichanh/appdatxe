# DoAn3 - Diagrams

Thuc muc chua cac diagram cho he thong **DoAn3 - Smart Ride Booking with AI Travel Assistant**.

## File chinh

| File | Mo ta |
|------|-------|
| `01_Class_Diagram_Android.puml` | Android Data Layer (SessionManager, Retrofit, Socket, DTOs, Repositories) |
| `02_Class_Diagram_Backend.puml` | Backend Structure (Middleware, Routes, Repositories, Utils) |
| `03_Class_Diagram_Database.puml` | Database Schema - 11 bang |
| `02_Sequence_Diagram.puml` | Sequence Diagram - 8 flows |
| `03_Communication_Diagram.puml` | Communication Diagram |
| `04_C4_Model.puml` | C4 Model |
| `cautrucdatabase.md` | Chi tiet 11 bang + quan he |

## 11 Bang Database

**Core Tables:**
1. `users` - Tai khoan nguoi dung
2. `drivers` - Ho so tai xe (1:1 voi users)
3. `rides` - Chuyen di
4. `driver_locations` - Vi tri GPS tai xe
5. `earnings` - Thu nhap tai xe

**AI Tables:**
6. `ai_trip_schedules` - Lich trinh AI
7. `ai_waypoints` - Diem dung
8. `ai_route_alternatives` - Tuyen thay the
9. `ai_learning_profiles` - Ho so hoc tap AI

**Batch Tables:**
10. `driver_route_batches` - Chuyen gom
11. `batch_passengers` - Hanh khach trong chuyen gom

## Chay local

```bash
cd docs/diagrams
run-plantuml.bat all
```

Hoac chi tiet:
```bash
run-plantuml.bat android
run-plantuml.bat backend
run-plantuml.bat database
run-plantuml.bat sequence
run-plantuml.bat communication
run-plantuml.bat c4
```

## PlantUML Online

Copy noi dung file `.puml` va paste vao: https://www.plantuml.com/plantuml/uml/
