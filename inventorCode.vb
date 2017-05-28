' refereces to incluede:
'                           Microsoft Scripting Framework
'							Autodesk Inventor Object Library


Sub SelectOuterEdgesOfConnectedFaces()
  Dim doc As PartDocument, p As Inventor.Point, prevP As Inventor.Point
  
  Dim ThisApplication As Inventor.Application
  
  Set ThisApplication = GetObject(, "Inventor.Application")
  
  Set doc = ThisApplication.ActiveDocument
  
  Dim f As Face, e As Edge
  
  Dim edges As ObjectCollection
    Dim tro As TransientObjects
    Set tro = ThisApplication.TransientObjects
    Set edges = tro.CreateObjectCollection
  
  If doc.SelectSet(1).Type = kFaceObject Then
    Set f = doc.SelectSet(1)
    Call doc.SelectSet.Clear
    
    Dim faces As ObjectCollection
    Set faces = tro.CreateObjectCollection
    
    Call GetAllTangentiallyConnectedFaces(f, faces)
        
    Call GetOuterEdgesOfFaces(faces, edges)
    Call doc.SelectSet.SelectMultiple(edges)
  End If
  
  ' if edges count = 0 (couldnt be calculated from face) lets assume edges are selected
  If edges.Count = 0 Then
    For i = 1 To doc.SelectSet.Count
      Set e = doc.SelectSet.Item(i)
      edges.Add e
    Next i
  End If
    
    Open "c:\Users\klemen\git\PipeCutter\NogaZgornji2.csv" For Output As #1
    
    Dim objUOM As UnitsOfMeasure
    Dim oCOM As Inventor.Point
    
    Dim oDoc As Inventor.Document
    Dim oWS As WorkSurface
    Dim oPart As Inventor.PartDocument
    Dim oCompDef As ComponentDefinition
    
    
    Set oPart = doc
    Set oCompDef = oPart.ComponentDefinition

    Set objUOM = oPart.UnitsOfMeasure
    Set oCOM = oCompDef.MassProperties.CenterOfMass
    
    maxX = oCompDef.RangeBox.MaxPoint.X
    maxY = oCompDef.RangeBox.MaxPoint.Y
    maxZ = oCompDef.RangeBox.MaxPoint.Z
    
    minX = oCompDef.RangeBox.MinPoint.X
    minY = oCompDef.RangeBox.MinPoint.Y
    minZ = oCompDef.RangeBox.MinPoint.Z

    centerX = ((maxX + minX) / 2) * 10
    centerY = ((maxY + minY) / 2) * 10
    centerZ = ((maxZ + minZ) / 2) * 10
    
    cogx = objUOM.GetStringFromValue(oCOM.X, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    cogy = objUOM.GetStringFromValue(oCOM.Y, UnitsTypeEnum.kDefaultDisplayLengthUnits)
    cogz = objUOM.GetStringFromValue(oCOM.Z, UnitsTypeEnum.kDefaultDisplayLengthUnits)
  
  
  For Each e In edges
    Set prevP = Nothing
    Set pline = ThisApplication.TransientGeometry.CreatePolyline3dFromCurve(e.Geometry, 0.001)
    pointsStr = ""
    For l = 1 To pline.PointCount
      pointNo = pointNo + 1
      Set p = pline.PointAtIndex(l)
      If (Not prevP Is Nothing) Then
        length = Math.Sqr((p.X - prevP.X) ^ 2 + (p.Y - prevP.Y) ^ 2 + (p.Z - prevP.Z) ^ 2)
        If length > 5 Then
            midX = (p.X + prevP.X) / 2
            midY = (p.Y + prevP.Y) / 2
            midZ = (p.Z + prevP.Z) / 2
            pointsStr = pointsStr & (midX * 10 - centerX) & ";" & (midY * 10 - centerY) & ";" & (midZ * 10 - centerZ) & ";"
        End If
      End If
      pointsStr = pointsStr & (p.X * 10 - centerX) & ";" & (p.Y * 10 - centerY) & ";" & (p.Z * 10 - centerZ) & ";"
      Set prevP = pline.PointAtIndex(l)
    Next l
    pointsStr = Left(pointsStr, Len(pointsStr) - 1)
    Print #1, "POLY_" & Trim(Str(surfaceNo)) & "_" & Trim(Str(edgeNo)) & "_" & pointNo & ";" & pointsStr
  Next
  Close #1
  
  MsgBox ("Ended")
End Sub




' Recursively collect all tangent faces
Sub GetAllTangentiallyConnectedFaces( _
f As Face, faces As ObjectCollection)
  Dim f2 As Face
  For Each f2 In f.TangentiallyConnectedFaces
    If Not IsInCollection(f2, faces) Then
      Call faces.Add(f2)
      Call GetAllTangentiallyConnectedFaces(f2, faces)
    End If
  Next
End Sub

' Check all edges, but ignore common
' edges with other faces
Sub GetOuterEdgesOfFaces( _
faces As ObjectCollection, edges As ObjectCollection)
  Dim f As Face
  For Each f In faces
    Dim e As Edge
    For Each e In f.edges
      Dim f2 As Face
      For Each f2 In e.faces
        If (Not f Is f2) And _
           (Not IsInCollection(f2, faces)) And _
           (Not IsInCollection(e, edges)) Then
          Call edges.Add(e)
        End If
      Next
    Next
  Next
End Sub