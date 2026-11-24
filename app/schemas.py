from pydantic import BaseModel, Field, EmailStr
from typing import Optional

class ComplaintCreate(BaseModel):
    reporter_name: str = Field(..., min_length=2, max_length=100)
    reporter_contact: Optional[str] = Field(None, max_length=100)
    area_id: int
    category: str = Field(..., pattern="^(Overflowing Bins|Littering|Plastic Waste|E-Waste|Hazardous Waste|Organic Waste)$")
    description: str = Field(..., min_length=5, max_length=1000)

class ComplaintUpdate(BaseModel):
    status: str = Field(..., pattern="^(Submitted|In Progress|Resolved)$")
    assigned_team: str = Field(..., min_length=2, max_length=100)
    remark: str = Field(..., min_length=2, max_length=500)

class AdminLogin(BaseModel):
    email: EmailStr
    password: str
